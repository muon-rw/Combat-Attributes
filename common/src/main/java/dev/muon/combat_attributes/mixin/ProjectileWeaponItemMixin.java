package dev.muon.combat_attributes.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.muon.combat_attributes.attribute.ModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Arrow-velocity and accuracy integration for both bows and crossbows. Hooks the
 * unified {@code ProjectileWeaponItem#shoot} entry point that both subclasses funnel
 * through, modifying the {@code power} (velocity) and {@code uncertainty} (spread)
 * parameters before they're passed down to per-projectile shooting.
 *
 * <p>Velocity scaling: {@code power *= (1 + arrow_velocity)}, additive percent bonus
 * on the muzzle speed. {@link net.minecraft.world.entity.projectile.arrow.AbstractArrow}'s
 * direct-hit damage is computed from the actual flight velocity, so this also indirectly
 * boosts arrow hit damage (on top of the {@code ranged_damage} flat bonus applied
 * via {@code LivingEntityHurtMixin}).
 *
 * <p>Accuracy scaling: {@code uncertainty *= max(0, 1 - accuracy)}. Perfect accuracy
 * (1.0) zeroes out spread; intermediate values reduce it proportionally.
 *
 */
@Mixin(value = ProjectileWeaponItem.class, remap = false)
public class ProjectileWeaponItemMixin {

    @ModifyVariable(method = "shoot", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float combat_attributes$scaleVelocity(float power, @Local(argsOnly = true, name = "shooter") LivingEntity shooter) {
        double arrowVelocity = ModAttributes.valueOrDefault(shooter, ModAttributes.arrowVelocity());
        if (arrowVelocity <= 0.0) return power;
        return (float) (power * (1.0 + arrowVelocity));
    }

    @ModifyVariable(method = "shoot", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private float combat_attributes$scaleAccuracy(float uncertainty, @Local(argsOnly = true, name = "shooter") LivingEntity shooter) {
        double accuracy = ModAttributes.valueOrDefault(shooter, ModAttributes.accuracy());
        if (accuracy <= 0.0) return uncertainty;
        return (float) (uncertainty * Math.max(0.0, 1.0 - accuracy));
    }
}
