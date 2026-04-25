package dev.muon.combat_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.muon.combat_attributes.attribute.ModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Bow draw-speed integration. Wraps the {@code getPowerForTime} call inside
 * {@code BowItem#releaseUsing} to scale the time-held argument upward by the shooter's
 * draw_speed attribute — vanilla then computes power as if the bow had been held
 * proportionally longer.
 *
 * <p>Example: with {@code draw_speed = 0.5}, holding the bow for 10 ticks yields the
 * power vanilla would compute for 15 ticks (10 × 1.5). Reaches max power at
 * {@code MAX_DRAW_DURATION / (1 + draw_speed)} ticks of real time.
 *
 * <p>Crossbow draw speed is handled in {@link CrossbowItemMixin}.
 *
 * <p>Shooter is pulled via {@code @Local(argsOnly = true)} rather than positional
 * trailing-arg capture, matching the pattern used in {@link ProjectileShootHelperMixin}
 * and {@link ProjectileWeaponItemMixin} for cross-loader portability.
 */
@Mixin(value = BowItem.class, remap = false)
public class BowItemMixin {

    @WrapOperation(
            method = "releaseUsing",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/BowItem;getPowerForTime(I)F")
    )
    private float combat_attributes$scaleDraw(int timeHeld, Operation<Float> original,
                                              @Local(argsOnly = true) LivingEntity entity) {
        double drawSpeed = ModAttributes.valueOrDefault(entity, ModAttributes.drawSpeed());
        if (drawSpeed <= 0.0) return original.call(timeHeld);
        int adjusted = (int) (timeHeld * (1.0 + drawSpeed));
        return original.call(adjusted);
    }
}
