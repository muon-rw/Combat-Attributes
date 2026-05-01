package dev.muon.combat_attributes.mixin.compat.appleskin;

import dev.muon.combat_attributes.feature.LegacyHunger;
import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Shrinks AppleSkin's tooltip overlay component height from 20 (two pip rows) to 10 (one heart
 * row) when legacy-hunger is on, so the tooltip doesn't reserve a blank line below our hearts.
 *
 * <p>Targets the NeoForge AppleSkin inner class {@code FoodTooltipRenderer}; Fabric AppleSkin
 * names it {@code FoodOverlay} (see the sibling mixin in the fabric module).
 */
@Mixin(targets = "squeek.appleskin.client.TooltipOverlayHandler$FoodTooltipRenderer", remap = false)
public abstract class FoodTooltipRendererMixin {

    @Inject(method = "getHeight", at = @At("HEAD"), cancellable = true)
    private void combat_attributes$shrinkToOneRow(Font font, CallbackInfoReturnable<Integer> cir) {
        if (LegacyHunger.isEnabled()) cir.setReturnValue(10);
    }
}
