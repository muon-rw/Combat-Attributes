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


@Mixin(value = LivingEntity.class, remap = false)
public abstract class LivingEntityMixin {

    @Shadow protected int useItemRemaining;

    @Inject(method = "updateUsingItem", at = @At("HEAD"), cancellable = true)
    private void combat_attributes$updateUsingItemHook(ItemStack useItem, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        boolean isRanged = useItem.getItem() instanceof ProjectileWeaponItem;
        boolean speedable = canBenefitFromDrawSpeed(useItem);
        if (!isRanged && !speedable) return;

        double drawSpeed = ModAttributes.valueOrDefault(self, ModAttributes.drawSpeed());
        int extras = drawSpeed != 0.0 ? combat_attributes$signedDrawSpeedDelta(self.tickCount, drawSpeed) : 0;

        if (isRanged && self instanceof Player player && !player.level().isClientSide()
                && !combat_attributes$tryDrainRangedDraw(player, useItem, extras)) {
            player.stopUsingItem();
            ci.cancel();
            return;
        }

        if (speedable && extras != 0) {
            this.useItemRemaining += extras;
        }
    }

    @Unique
    private boolean combat_attributes$tryDrainRangedDraw(Player player, ItemStack useItem, int extras) {
        LivingEntity self = (LivingEntity) (Object) this;
        int chargeDuration = combat_attributes$chargeDurationTicks(useItem, self);
        int effectiveTicksElapsed = useItem.getUseDuration(self) - this.useItemRemaining;
        int progressThisTick = 1 - extras;
        if (effectiveTicksElapsed >= chargeDuration || progressThisTick <= 0) return true;
        float baseCost = Configs.GENERAL.rangedDrawStaminaCost.get().floatValue();
        float drain = (baseCost / (float) chargeDuration) * progressThisTick;
        return drain <= 0.0F || PlayerResources.trySpendStamina(player, drain);
    }

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
     * {@code getUseDuration}, which for vanilla-shaped items defaults to 72000
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

    @Unique
    private static int combat_attributes$signedDrawSpeedDelta(int tickCount, double drawSpeed) {
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
