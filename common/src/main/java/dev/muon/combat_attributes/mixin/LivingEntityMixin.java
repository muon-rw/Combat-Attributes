package dev.muon.combat_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.muon.combat_attributes.attribute.ModAttributes;
import dev.muon.combat_attributes.config.Configs;
import dev.muon.combat_attributes.damage.DamageHandler;
import dev.muon.combat_attributes.resource.PlayerResources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * {@link LivingEntity}-side hooks for three subsystems:
 *
 * <ul>
 *   <li><b>Use-item update</b> — combined draw_speed acceleration and per-tick
 *       ranged stamina drain on {@code updateUsingItem}. They share a tick of
 *       {@code useItemRemaining} state, so they live in one inject to avoid
 *       intra-class injector ordering surprises.</li>
 *   <li><b>Jump</b> — stamina gate on {@code jumpFromGround} (which lives on
 *       {@link LivingEntity} in this version, no {@link Player} override).</li>
 *   <li><b>Damage pipeline</b> — {@link DamageHandler}-driven evasion, ranged /
 *       crit modification, and lifesteal on {@code hurtServer}.</li>
 * </ul>
 */
@Mixin(value = LivingEntity.class, remap = false)
public abstract class LivingEntityMixin {

    @Shadow protected int useItemRemaining;

    /**
     * Combined per-tick hook on {@code updateUsingItem}:
     *
     * <ol>
     *   <li>Stamina-aware ranged-weapon gate (server, players only). Each effective
     *       use-item tick (vanilla's −1 plus this mod's {@code draw_speed} extras)
     *       maps to one stamina-drain unit; total cost from start to full charge is
     *       approximately {@code rangedDrawStaminaCost}, regardless of
     *       {@code draw_speed}. When stamina is in the post-exhaustion lockout, the
     *       attempt fails: the use is aborted via {@link Player#stopUsingItem()}
     *       and the rest of this tick's update is cancelled. Past full charge —
     *       20 ticks for {@link BowItem}, {@link CrossbowItem#getChargeDuration}
     *       for crossbows, {@code getUseDuration} for mod ranged weapons — the
     *       drain stops; the regen-pause that pins stamina at its current level
     *       is applied separately by {@code PlayerResourceTicker} for as long as
     *       the player is using the item.</li>
     *   <li>{@code draw_speed} acceleration (any LivingEntity using
     *       {@link ProjectileWeaponItem} or {@link TridentItem}). Mirrors Apothic
     *       Attributes' event-based handler — every full point adds one extra
     *       decrement per tick; partial points spread their extra across game ticks
     *       via {@code tickCount}-modulo gating. Negative values invert the sign to
     *       slow the timer down.</li>
     * </ol>
     */
    @Inject(method = "updateUsingItem", at = @At("HEAD"), cancellable = true)
    private void combat_attributes$updateUsingItemHook(ItemStack useItem, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        boolean isRanged = useItem.getItem() instanceof ProjectileWeaponItem;
        boolean speedable = canBenefitFromDrawSpeed(useItem);
        if (!isRanged && !speedable) return;

        double drawSpeed = ModAttributes.valueOrDefault(self, ModAttributes.drawSpeed());
        int extras = drawSpeed != 0.0 ? combat_attributes$drawSpeedExtras(self.tickCount, drawSpeed) : 0;

        // Phase 1: server-side ranged stamina (lockout block + per-tick charge-phase drain).
        if (isRanged && self instanceof Player player && !player.level().isClientSide()) {
            int chargeDuration = combat_attributes$chargeDurationTicks(useItem, self);
            int effectiveTicksElapsed = useItem.getUseDuration(self) - this.useItemRemaining;
            int progressThisTick = 1 - extras;

            if (effectiveTicksElapsed < chargeDuration && progressThisTick > 0) {
                float baseCost = Configs.GENERAL.rangedDrawStaminaCost.get().floatValue();
                float drain = (baseCost / (float) chargeDuration) * progressThisTick;
                if (drain > 0.0F && !PlayerResources.trySpendStamina(player, drain)) {
                    player.stopUsingItem();
                    ci.cancel();
                    return;
                }
            }
        }

        // Phase 2: draw_speed acceleration.
        if (speedable && extras != 0) {
            this.useItemRemaining += extras;
        }
    }

    /**
     * Stamina gate for ground jumps. Drains {@code jumpStaminaCost} on success;
     * cancels the jump entirely when stamina is in the lockout window. Mob jumps
     * (any non-{@link Player} {@link LivingEntity}) pass through untouched —
     * stamina attributes are player-only.
     */
    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void combat_attributes$jumpStaminaGate(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        float cost = Configs.GENERAL.jumpStaminaCost.get().floatValue();
        if (!PlayerResources.trySpendStamina(player, cost)) {
            ci.cancel();
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
    private static final int BOW_FULL_CHARGE_TICKS = 20;

    @Unique
    private static boolean canBenefitFromDrawSpeed(ItemStack stack) {
        return stack.getItem() instanceof ProjectileWeaponItem || stack.getItem() instanceof TridentItem;
    }

    /**
     * Item-specific full-charge time used as the denominator for spreading
     * {@code rangedDrawStaminaCost} across the use phase. Bow is hardcoded to
     * vanilla's 20-tick power curve; crossbow defers to vanilla's
     * {@link CrossbowItem#getChargeDuration} so the {@code quick_charge}
     * enchantment shortens the drain window proportionally. Mod ranged weapons
     * extending {@link ProjectileWeaponItem} fall back to their declared
     * {@code getUseDuration} — which for vanilla-shaped items defaults to 72000
     * and makes drain effectively a no-op, so a mod author who wants integration
     * can register a {@code ChangeStaminaEvent} listener of their own.
     */
    @Unique
    private static int combat_attributes$chargeDurationTicks(ItemStack stack, LivingEntity user) {
        Item item = stack.getItem();
        if (item instanceof BowItem) return BOW_FULL_CHARGE_TICKS;
        if (item instanceof CrossbowItem) return Math.max(1, CrossbowItem.getChargeDuration(stack, user));
        return Math.max(1, stack.getUseDuration(user));
    }

    /**
     * Signed total decrement to apply to {@code useItemRemaining} this tick from
     * the {@code draw_speed} attribute. Negative for positive {@code drawSpeed}
     * (faster); positive for negative {@code drawSpeed} (slower); zero when
     * {@code drawSpeed == 0}. Pure function of {@code tickCount} and
     * {@code drawSpeed} so both the bow-drain progress accounting and the
     * acceleration write can read the same value without ordering coupling.
     */
    @Unique
    private static int combat_attributes$drawSpeedExtras(int tickCount, double drawSpeed) {
        if (drawSpeed == 0.0) return 0;
        int offset = drawSpeed > 0.0 ? -1 : 1;
        double t = Math.abs(drawSpeed);

        int total = 0;
        while (t > 1.0) {
            total += offset;
            t -= 1.0;
        }
        if (t > 0.5) {
            if (tickCount % 2 == 0) total += offset;
            t -= 0.5;
        }
        if (t > 0.0) {
            int mod = (int) Math.floor(1.0 / Math.min(1.0, t));
            if (mod > 0 && tickCount % mod == 0) total += offset;
        }
        return total;
    }
}
