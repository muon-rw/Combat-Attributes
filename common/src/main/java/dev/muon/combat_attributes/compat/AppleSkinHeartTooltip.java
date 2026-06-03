package dev.muon.combat_attributes.compat;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.muon.combat_attributes.client.HudBars;
import dev.muon.combat_attributes.feature.LegacyHunger;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.food.FoodProperties;

/**
 * Replaces AppleSkin's food/saturation tooltip overlay with a hearts row that previews the
 * {@link LegacyHunger#computeHeal heal amount} the legacy-hunger feature applies on consume.
 *
 * <p>Loader entry points hook {@code TooltipOverlayEvent.Render}, then call {@link #draw} at the
 * (x, y) AppleSkin computed for its overlay and cancel the event so AppleSkin's vanilla pip
 * rendering is suppressed. AppleSkin's two relevant config toggles
 * ({@code showFoodValuesInTooltip}, Shift-to-show, and {@code showFoodValuesInTooltipAlways})
 * gate whether AppleSkin fires {@code Render} at all, so listening at {@code Render} inherits
 * both behaviors automatically without us reading AppleSkin's config directly.
 */
public final class AppleSkinHeartTooltip {

    public static final String MOD_ID = "appleskin";

    private static final Identifier HEART_FULL      = Identifier.parse("hud/heart/full");
    private static final Identifier HEART_HALF      = Identifier.parse("hud/heart/half");
    private static final Identifier HEART_CONTAINER = Identifier.parse("hud/heart/container");

    private static final int MAX_HEARTS = 10;

    private AppleSkinHeartTooltip() {}

    /** @return true if the event should be cancelled (we drew); false leaves AppleSkin's render. */
    public static boolean draw(GuiGraphicsExtractor graphics, int x, int y, FoodProperties food) {
        if (!LegacyHunger.isEnabled() || food == null) return false;

        int halves = (int) LegacyHunger.computeHeal(food);
        if (halves <= 0) {
            // Even at zero we still cancel so AppleSkin doesn't draw food pips on top.
            return true;
        }

        int hearts = Math.min(MAX_HEARTS, (halves + 1) / 2);
        RenderPipeline pipeline = RenderPipelines.GUI_TEXTURED;
        for (int i = 0; i < hearts; i++) {
            int xo = x + i * HudBars.PIP_STRIDE;
            graphics.blitSprite(pipeline, HEART_CONTAINER, xo, y, HudBars.SPRITE_SIZE, HudBars.SPRITE_SIZE);
            int firstHalfIndex = i * 2 + 1;
            if (firstHalfIndex < halves) {
                graphics.blitSprite(pipeline, HEART_FULL, xo, y, HudBars.SPRITE_SIZE, HudBars.SPRITE_SIZE);
            } else if (firstHalfIndex == halves) {
                graphics.blitSprite(pipeline, HEART_HALF, xo, y, HudBars.SPRITE_SIZE, HudBars.SPRITE_SIZE);
            }
        }
        return true;
    }
}
