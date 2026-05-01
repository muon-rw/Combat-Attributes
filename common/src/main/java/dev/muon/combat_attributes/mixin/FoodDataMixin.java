package dev.muon.combat_attributes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.muon.combat_attributes.feature.LegacyHunger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Three changes to {@link FoodData} when legacy-hunger is on:
 * <ul>
 *   <li>{@link FoodData#getFoodLevel} is clamped to {@code [7, 17]}. The lower bound (7) sits one
 *       above vanilla's no-sprint cutoff (≤6) and clear of the starvation threshold; the upper
 *       bound (17) is the highest value that still satisfies AppleSkin's
 *       {@code shouldShowEstimatedHealth} gate ({@code foodLevel < 18}), so the held-food heart
 *       preview keeps rendering even when the underlying field is at max.</li>
 *   <li>{@link FoodData#needsFood} always reports true, so non-{@code canAlwaysEat} foods stay
 *       eligible to consume regardless of the underlying foodLevel. Vanilla's read is
 *       {@code foodLevel < 20} against the field directly, bypassing the getter — and AppleSkin's
 *       held-food preview gates on this through {@code Player.canEat}, so without the override
 *       the heart preview never renders for normal foods at full hunger.</li>
 *   <li>{@link FoodData#tick} is short-circuited. The vanilla tick reads {@code foodLevel} and
 *       {@code saturationLevel} as fields (bypassing our getter), so it would still apply natural
 *       regen at high field values and starvation damage at zero — the on-eat heal already
 *       handles healing, and the food field is meaningless under legacy-hunger.</li>
 * </ul>
 */
@Mixin(value = FoodData.class, remap = false)
public abstract class FoodDataMixin {

    @ModifyReturnValue(method = "getFoodLevel", at = @At("RETURN"))
    private int combat_attributes$clampFoodLevel(int original) {
        if (!LegacyHunger.isEnabled()) return original;
        if (original < 7) return 7;
        if (original > 17) return 17;
        return original;
    }

    @ModifyReturnValue(method = "needsFood", at = @At("RETURN"))
    private boolean combat_attributes$alwaysNeedsFood(boolean original) {
        return original || LegacyHunger.isEnabled();
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void combat_attributes$skipTick(ServerPlayer player, CallbackInfo ci) {
        if (LegacyHunger.isEnabled()) ci.cancel();
    }
}
