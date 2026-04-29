package dev.muon.combat_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.muon.combat_attributes.attribute.ModAttributes;
import dev.muon.combat_attributes.damage.DamageHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = LivingEntity.class, remap = false)
public abstract class LivingEntityMixin {

    @Shadow protected int useItemRemaining;

    /**
     * Implementation of {@code draw_speed} for bows, crossbows, and tridents. Each tick the
     * entity is using one of those items, this drains additional {@code useItemRemaining} on
     * top of vanilla's natural decrement, so the underlying use timer advances faster — and
     * everything keyed off it (bow draw animation, crossbow charge threshold, trident throw
     * threshold, and the {@code timeHeld} value passed to {@code releaseUsing}) speeds up
     * uniformly. One mixin, three weapons, no per-item integration.
     *
     * <p>Algorithm mirrors Apothic Attributes' event-based handler: every full
     * point of draw_speed adds one extra decrement per tick, and partial points spread their
     * extra decrement across game ticks via {@code tickCount}-modulo gating. Negative
     * draw_speed inverts the sign to slow the use timer down — preserved from the reference
     * impl in case a server config or modifier dips below zero.
     *
     * <p>Could theoretically be extended to more items.</p>
     */
    @Inject(method = "updateUsingItem", at = @At("HEAD"))
    private void combat_attributes$applyDrawSpeed(ItemStack useItem, CallbackInfo ci) {
        if (!canBenefitFromDrawSpeed(useItem)) return;
        LivingEntity self = (LivingEntity) (Object) this;
        double drawSpeed = ModAttributes.valueOrDefault(self, ModAttributes.drawSpeed());
        if (drawSpeed == 0.0) return;

        double t = drawSpeed;
        int offset = -1;
        if (t < 0.0) {
            offset = 1;
            t = -t;
        }

        // Each whole point of draw_speed contributes one full extra decrement per tick.
        while (t > 1.0) {
            this.useItemRemaining += offset;
            t -= 1.0;
        }

        // (0.5, 1.0]: extra decrement every 2 ticks. Special-cased so values just under 1
        // don't collapse to mod=1 (which would mean an extra decrement *every* tick).
        if (t > 0.5) {
            if (self.tickCount % 2 == 0) this.useItemRemaining += offset;
            t -= 0.5;
        }

        // (0, 0.5]: extra decrement every floor(1/t) ticks.
        if (t > 0.0) {
            int mod = (int) Math.floor(1.0 / Math.min(1.0, t));
            if (mod > 0 && self.tickCount % mod == 0) this.useItemRemaining += offset;
        }
    }

    /**
     * Attribute hooks:
     * <ol>
     *   <li>Evasion — if the victim dodges, skip the original method entirely (returns
     *       {@code false}; vanilla treats this as no damage applied).</li>
     *   <li>Modify incoming damage — ranged flat bonus + crit multipliers per damage type.</li>
     *   <li>Call vanilla {@code hurtServer} with the modified amount.</li>
     *   <li>Lifesteal — if vanilla returned {@code true} (damage applied) AND the attacker is
     *       within their {@code entity_interaction_range} of the victim, heal the attacker.</li>
     * </ol>
     */
    @WrapMethod(method = "hurtServer")
    private boolean combat_attributes$wrapHurt(ServerLevel level, DamageSource source, float damage,
                                               Operation<Boolean> original) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (DamageHandler.shouldDodge(self, source)) {
            return false;
        }

        float modified = DamageHandler.modifyIncomingDamage(self, source, damage);
        boolean applied = original.call(level, source, modified);

        if (applied) {
            DamageHandler.afterDamage(self, source, modified);
        }
        return applied;
    }


    @Unique
    private static boolean canBenefitFromDrawSpeed(ItemStack stack) {
        return stack.getItem() instanceof ProjectileWeaponItem || stack.getItem() instanceof TridentItem;
    }
}
