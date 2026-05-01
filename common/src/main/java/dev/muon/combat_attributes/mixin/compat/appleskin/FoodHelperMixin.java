package dev.muon.combat_attributes.mixin.compat.appleskin;

import dev.muon.combat_attributes.feature.LegacyHunger;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import squeek.appleskin.helpers.ConsumableFood;
import squeek.appleskin.helpers.FoodHelper;

/**
 * Redirects AppleSkin's public estimated-health-restore lookup to {@link LegacyHunger#computeHeal}
 * when legacy-hunger is on. AppleSkin's HUD held-food preview and any third-party caller (notably
 * Dynamic Resource Bars) read through this method, so the on-eat preview always matches what
 * {@link Player#heal} actually applies. Returns 0 at full health to mirror AppleSkin's "no preview
 * when no heal would land" behavior.
 *
 * <p>Lives in common: {@code FoodHelper} / {@code ConsumableFood} are byte-compatible across both
 * loader variants of AppleSkin, and the per-loader mixin configs gate this on {@code isModLoaded}
 * via the {@code mixin/compat/appleskin/} package convention.
 */
@Mixin(value = FoodHelper.class, remap = false)
@SuppressWarnings("all") // Mixin applied per loader, not added to common mixin json for conditional loading
public abstract class FoodHelperMixin {

    @Inject(method = "getEstimatedHealthIncrement(Lnet/minecraft/world/entity/player/Player;Lsqueek/appleskin/helpers/ConsumableFood;)F",
            at = @At("HEAD"), cancellable = true)
    private static void combat_attributes$legacyHungerHeal(Player player, ConsumableFood food,
                                                           CallbackInfoReturnable<Float> cir) {
        if (!LegacyHunger.isEnabled()) return;
        if (!player.isHurt()) {
            cir.setReturnValue(0f);
            return;
        }
        float missing = player.getMaxHealth() - player.getHealth();
        cir.setReturnValue(Math.min(missing, LegacyHunger.computeHeal(food.food())));
    }
}
