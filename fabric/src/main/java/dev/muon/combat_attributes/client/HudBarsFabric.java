package dev.muon.combat_attributes.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudStatusBarHeightRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;

/**
 * Fabric HUD wiring for the stamina + mana bars.
 *
 * <p>Two registrations per bar:
 * <ol>
 *   <li>{@link HudElementRegistry#attachElementAfter} — places the bar in render
 *       order just above the food bar (then mana above stamina). The "after"
 *       direction maps to "stacked above" on screen because the food bar reads
 *       its own Y offset from the height registry below.</li>
 *   <li>{@link HudStatusBarHeightRegistry#addRight} — declares how tall the
 *       bar is. Returning {@code 0} when {@link HudBars#shouldShowStamina}
 *       reports false is what makes the air bar shift only when our bars are
 *       actually drawn — Fabric API consumes this same registry to compute
 *       vanilla bars' Y offsets, so no mixin is needed for the dynamic shift.
 *       (See {@code HudStatusBarHeightRegistry} javadoc, lines 33-46 in 23.0.4.)</li>
 * </ol>
 *
 * <p>The Y coordinate handed to {@link HudBars#renderStamina} is computed as
 * {@code guiHeight() - getHeight(STAMINA_ELEMENT)} — the registry returns
 * {@code 39 + sumOfBarsBelow}, so subtracting it yields the top edge of our row.
 * Mana sits another bar-height up by the same trick.
 */
public final class HudBarsFabric {

    private HudBarsFabric() {}

    public static void initClient() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.FOOD_BAR, HudBars.STAMINA_ELEMENT,
                (graphics, deltaTracker) -> {
                    var player = Minecraft.getInstance().player;
                    if (player == null) return;
                    int y = graphics.guiHeight() - HudStatusBarHeightRegistry.getHeight(HudBars.STAMINA_ELEMENT);
                    HudBars.renderStamina(graphics, player, y);
                });
        HudElementRegistry.attachElementAfter(HudBars.STAMINA_ELEMENT, HudBars.MANA_ELEMENT,
                (graphics, deltaTracker) -> {
                    var player = Minecraft.getInstance().player;
                    if (player == null) return;
                    int y = graphics.guiHeight() - HudStatusBarHeightRegistry.getHeight(HudBars.MANA_ELEMENT);
                    HudBars.renderMana(graphics, player, y);
                });

        HudStatusBarHeightRegistry.addRight(HudBars.STAMINA_ELEMENT,
                player -> HudBars.shouldShowStamina(player) ? HudBars.BAR_HEIGHT : 0);
        HudStatusBarHeightRegistry.addRight(HudBars.MANA_ELEMENT,
                player -> HudBars.shouldShowMana(player) ? HudBars.BAR_HEIGHT : 0);
    }
}
