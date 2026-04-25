package dev.muon.combat_attributes.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.muon.combat_attributes.attribute.ModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Extends {@code arrow_velocity} and {@code accuracy} to mob ranged attacks. Vanilla
 * mobs that bypass {@code ProjectileWeaponItem#shoot} call
 * {@link Projectile#spawnProjectileUsingShoot} directly with hardcoded velocity /
 * inaccuracy. Both overloads of that helper end at {@code projectile.shoot(x, y, z, pow,
 * uncertainty)}; we hook each at HEAD and apply the same scaling as the player path.
 *
 * <ul>
 *   <li><b>Direct overload</b> ({@code (Projectile, ServerLevel, ItemStack, ..., float pow,
 *       float uncertainty)}): used by AbstractSkeleton, Drowned. Shooter comes from
 *       {@code projectile.getOwner()}.</li>
 *   <li><b>Factory overload</b> ({@code (ProjectileFactory, ServerLevel, ItemStack,
 *       LivingEntity source, ..., float pow, float uncertainty)}): used by Llama spit,
 *       Illusioner, Breeze, etc. Shooter comes from the explicit {@code source} arg,
 *       captured here via MixinExtras' {@code @Local(argsOnly = true)} — that avoids
 *       overloading the handler with positional args, which seems to be what triggered
 *       NeoForge's "Scanned 0 target(s)" rejection on the prior unified version.</li>
 * </ul>
 *
 * <p>Player-driven {@code ProjectileWeaponItem#shoot} doesn't go through either overload
 * — it routes via {@code Projectile.spawnProjectile} (no {@code UsingShoot} suffix) and
 * is already covered by {@link ProjectileWeaponItemMixin}. So no double-application risk.
 */
@Mixin(value = Projectile.class, remap = false)
public class ProjectileShootHelperMixin {

    // --- Direct overload: shooter from projectile.getOwner() ---

    @ModifyVariable(
            method = "spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;",
            at = @At("HEAD"), argsOnly = true, name = "pow")
    private static float combat_attributes$scaleVelocity_direct(float pow,
                                                                @Local(argsOnly = true, name = "projectile") Projectile projectile) {
        return projectile.getOwner() instanceof LivingEntity owner ? scaleVelocity(owner, pow) : pow;
    }

    @ModifyVariable(
            method = "spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;",
            at = @At("HEAD"), argsOnly = true, name = "uncertainty")
    private static float combat_attributes$scaleAccuracy_direct(float uncertainty,
                                                                @Local(argsOnly = true, name = "projectile") Projectile projectile) {
        return projectile.getOwner() instanceof LivingEntity owner ? scaleAccuracy(owner, uncertainty) : uncertainty;
    }

    // --- Factory overload: shooter from explicit `source` arg ---

    @ModifyVariable(
            method = "spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;",
            at = @At("HEAD"), argsOnly = true, name = "pow")
    private static float combat_attributes$scaleVelocity_factory(float pow,
                                                                 @Local(argsOnly = true, name = "source") LivingEntity source) {
        return scaleVelocity(source, pow);
    }

    @ModifyVariable(
            method = "spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;",
            at = @At("HEAD"), argsOnly = true, name = "uncertainty")
    private static float combat_attributes$scaleAccuracy_factory(float uncertainty,
                                                                 @Local(argsOnly = true, name = "source") LivingEntity source) {
        return scaleAccuracy(source, uncertainty);
    }

    @Unique
    private static float scaleVelocity(LivingEntity shooter, float pow) {
        double bonus = ModAttributes.valueOrDefault(shooter, ModAttributes.arrowVelocity());
        if (bonus <= 0.0) return pow;
        return (float) (pow * (1.0 + bonus));
    }

    @Unique
    private static float scaleAccuracy(LivingEntity shooter, float uncertainty) {
        double bonus = ModAttributes.valueOrDefault(shooter, ModAttributes.accuracy());
        if (bonus <= 0.0) return uncertainty;
        return (float) (uncertainty * Math.max(0.0, 1.0 - bonus));
    }
}
