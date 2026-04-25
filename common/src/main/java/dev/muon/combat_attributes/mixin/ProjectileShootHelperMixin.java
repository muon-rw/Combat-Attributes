package dev.muon.combat_attributes.mixin;

import dev.muon.combat_attributes.attribute.ModAttributes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Extends {@code arrow_velocity} and {@code accuracy} to mob ranged attacks. Vanilla
 * mobs (AbstractSkeleton, Drowned, etc.) bypass {@code ProjectileWeaponItem#shoot} and
 * call {@link Projectile#spawnProjectileUsingShoot} directly with hardcoded velocity /
 * inaccuracy. Both overloads ultimately invoke {@code projectile.shoot(x, y, z, pow,
 * uncertainty)}; we hook the {@code pow} and {@code uncertainty} args at HEAD of each
 * overload and apply the same scaling as the player path.
 *
 * <ul>
 *   <li><b>Overload 1</b>: takes a {@code ProjectileFactory} and an explicit
 *       {@code LivingEntity source}. Used by callers that want the helper to construct
 *       the projectile.</li>
 *   <li><b>Overload 2</b>: takes a pre-constructed {@code Projectile}; the shooter is
 *       read from {@code projectile.getOwner()}. This is the path AbstractSkeleton and
 *       Drowned both use.</li>
 * </ul>
 *
 * <p>Player-driven {@code ProjectileWeaponItem#shoot} doesn't go through this helper —
 * it routes via {@code Projectile.spawnProjectile} (no {@code UsingShoot} suffix) and is
 * already covered by {@link ProjectileWeaponItemMixin}. So no double-application risk.
 */
@Mixin(value = Projectile.class, remap = false)
public class ProjectileShootHelperMixin {

    // --- Overload 1: factory + explicit LivingEntity source ---

    @ModifyVariable(
            method = "spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;",
            at = @At("HEAD"), argsOnly = true, name = "pow")
    private static float combat_attributes$scaleVelocity_factory(float pow,
                                                                 Projectile.ProjectileFactory<?> creator,
                                                                 ServerLevel serverLevel,
                                                                 ItemStack itemStack,
                                                                 LivingEntity source) {
        return scaleVelocity(source, pow);
    }

    @ModifyVariable(
            method = "spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;",
            at = @At("HEAD"), argsOnly = true, name = "uncertainty")
    private static float combat_attributes$scaleAccuracy_factory(float uncertainty,
                                                                 Projectile.ProjectileFactory<?> creator,
                                                                 ServerLevel serverLevel,
                                                                 ItemStack itemStack,
                                                                 LivingEntity source) {
        return scaleAccuracy(source, uncertainty);
    }

    // --- Overload 2: pre-constructed projectile, shooter from projectile.getOwner() ---

    @ModifyVariable(
            method = "spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;",
            at = @At("HEAD"), argsOnly = true, name = "pow")
    private static float combat_attributes$scaleVelocity_direct(float pow,
                                                                Projectile projectile) {
        return projectile.getOwner() instanceof LivingEntity owner ? scaleVelocity(owner, pow) : pow;
    }

    @ModifyVariable(
            method = "spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;",
            at = @At("HEAD"), argsOnly = true, name = "uncertainty")
    private static float combat_attributes$scaleAccuracy_direct(float uncertainty,
                                                                Projectile projectile) {
        return projectile.getOwner() instanceof LivingEntity owner ? scaleAccuracy(owner, uncertainty) : uncertainty;
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
