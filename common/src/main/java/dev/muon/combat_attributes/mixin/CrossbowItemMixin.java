package dev.muon.combat_attributes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.muon.combat_attributes.attribute.ModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Crossbow charge-speed integration. Divides {@code CrossbowItem.getChargeDuration}'s
 * return value by {@code (1 + draw_speed)}, stacking on top of vanilla's
 * {@code QUICK_CHARGE} enchantment effect (which is already applied inside the method
 * via {@code EnchantmentHelper.modifyCrossbowChargingTime}).
 *
 * <p>Example: with {@code draw_speed = 0.5} and no enchantments, the 25-tick base
 * charge becomes ~16 ticks. Floor of 1 tick to avoid division weirdness.
 *
 * <p>This method gets called from {@code HumanoidMobRenderer} for any humanoid mob
 * holding any item — including mobs whose {@code AttributeSupplier} might not have
 * draw_speed if a stale supplier somehow survives. {@link ModAttributes#valueOrDefault}
 * returns the intrinsic default rather than throwing in that case.
 */
@Mixin(value = CrossbowItem.class, remap = false)
public class CrossbowItemMixin {

    @ModifyReturnValue(method = "getChargeDuration", at = @At("RETURN"))
    private static int combat_attributes$scaleChargeDuration(int original,
                                                             ItemStack crossbow,
                                                             LivingEntity user) {
        double drawSpeed = ModAttributes.valueOrDefault(user, ModAttributes.drawSpeed());
        if (drawSpeed <= 0.0) return original;
        return Math.max(1, (int) (original / (1.0 + drawSpeed)));
    }
}
