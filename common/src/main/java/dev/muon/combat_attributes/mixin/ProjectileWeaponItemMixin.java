package dev.muon.combat_attributes.mixin;

import dev.muon.combat_attributes.attribute.ModAttributes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

/**
 * Arrow-velocity and accuracy integration for both bows and crossbows. Hooks the
 * unified {@code ProjectileWeaponItem#shoot} entry point that both subclasses funnel
 * through, modifying the {@code power} (velocity) and {@code uncertainty} (spread)
 * parameters before they're passed down to per-projectile shooting.
 *
 * <p>Velocity scaling: {@code power *= (1 + arrow_velocity)} — additive percent bonus
 * on the muzzle speed. Note that {@link net.minecraft.world.entity.projectile.AbstractArrow}'s
 * direct-hit damage is computed from the actual flight velocity, so this also indirectly
 * boosts arrow hit damage (in addition to the {@code ranged_damage} flat bonus applied
 * via {@code LivingEntityHurtMixin}).
 *
 * <p>Accuracy scaling: {@code uncertainty *= max(0, 1 - accuracy)} — perfect accuracy
 * (1.0) zeroes out spread; intermediate values reduce it proportionally.
 */
@Mixin(value = ProjectileWeaponItem.class, remap = false)
public class ProjectileWeaponItemMixin {

    @ModifyVariable(method = "shoot", at = @At("HEAD"), argsOnly = true, name = "power")
    private float combat_attributes$scaleVelocity(float power,
                                                  ServerLevel level, LivingEntity shooter,
                                                  InteractionHand hand, ItemStack weapon,
                                                  List<ItemStack> projectiles) {
        double arrowVelocity = ModAttributes.valueOrDefault(shooter, ModAttributes.arrowVelocity());
        if (arrowVelocity <= 0.0) return power;
        return (float) (power * (1.0 + arrowVelocity));
    }

    @ModifyVariable(method = "shoot", at = @At("HEAD"), argsOnly = true, name = "uncertainty")
    private float combat_attributes$scaleAccuracy(float uncertainty,
                                                  ServerLevel level, LivingEntity shooter,
                                                  InteractionHand hand, ItemStack weapon,
                                                  List<ItemStack> projectiles) {
        double accuracy = ModAttributes.valueOrDefault(shooter, ModAttributes.accuracy());
        if (accuracy <= 0.0) return uncertainty;
        return (float) (uncertainty * Math.max(0.0, 1.0 - accuracy));
    }
}
