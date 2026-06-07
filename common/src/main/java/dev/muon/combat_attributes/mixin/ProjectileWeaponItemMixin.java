package dev.muon.combat_attributes.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.muon.combat_attributes.attribute.ModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// Velocity scaling also boosts AbstractArrow direct-hit damage, since vanilla derives hit damage from flight velocity.
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
