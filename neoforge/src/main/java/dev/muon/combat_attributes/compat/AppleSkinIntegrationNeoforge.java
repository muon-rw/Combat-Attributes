package dev.muon.combat_attributes.compat;

import dev.muon.combat_attributes.feature.LegacyHunger;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import squeek.appleskin.api.event.HUDOverlayEvent;
import squeek.appleskin.api.event.TooltipOverlayEvent;

/**
 * NeoForge AppleSkin hooks. Replaces AppleSkin's tooltip food/saturation overlay with hearts and,
 * when legacy-hunger is on, suppresses the HUD overlays AppleSkin draws on top of the (now hidden)
 * vanilla hunger bar — held-food preview, saturation overlay, exhaustion overlay.
 * {@link HUDOverlayEvent.HealthRestored} is intentionally left alone: it's the held-food heart
 * preview, which under legacy-hunger reflects {@link LegacyHunger#computeHeal} via
 * {@code FoodHelperMixin} and stays visible.
 *
 * <p>Manually registered (not {@code @EventBusSubscriber}) so the AppleSkin types only class-load
 * after the {@code isModLoaded("appleskin")} gate in {@code CombatAttributesNeoforge}.
 *
 * <p>Fabric doesn't need the hunger-row suppressors: replacing {@code VanillaHudElements.FOOD_BAR}
 * wholesale already swallows AppleSkin's overlay path. NeoForge AppleSkin registers its overlays
 * as separate GUI layers, so wrapping {@code FOOD_LEVEL} doesn't reach them.
 */
public final class AppleSkinIntegrationNeoforge {

    private AppleSkinIntegrationNeoforge() {}

    public static void init() {
        NeoForge.EVENT_BUS.addListener(TooltipOverlayEvent.Render.class, event -> {
            if (AppleSkinHeartTooltip.draw(event.guiGraphics, event.x, event.y, event.modifiedFood)) {
                event.setCanceled(true);
            }
        });
        cancelWhenLegacy(HUDOverlayEvent.HungerRestored.class);
        cancelWhenLegacy(HUDOverlayEvent.Saturation.class);
        cancelWhenLegacy(HUDOverlayEvent.Exhaustion.class);
    }

    private static <E extends Event & ICancellableEvent> void cancelWhenLegacy(Class<E> type) {
        NeoForge.EVENT_BUS.addListener(type, event -> {
            if (LegacyHunger.isEnabled()) event.setCanceled(true);
        });
    }
}
