package dev.muon.combat_attributes.compat;

import squeek.appleskin.api.event.TooltipOverlayEvent;

/**
 * Fabric AppleSkin tooltip hook. Listens to {@code TooltipOverlayEvent.Render} and replaces the
 * food/saturation pip overlay with a heart row when legacy-hunger is on.
 *
 * <p>Class-load-gated: callers must {@code isModLoaded("appleskin")} before touching this class
 * so the AppleSkin types stay off the verifier path on installs without it.
 */
public final class AppleSkinIntegrationFabric {

    private AppleSkinIntegrationFabric() {}

    public static void init() {
        TooltipOverlayEvent.Render.EVENT.register(event -> {
            if (AppleSkinHeartTooltip.draw(event.context, event.x, event.y, event.modifiedFood)) {
                event.isCanceled = true;
            }
        });
    }
}
