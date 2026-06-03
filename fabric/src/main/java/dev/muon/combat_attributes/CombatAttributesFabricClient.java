package dev.muon.combat_attributes;

import dev.muon.combat_attributes.client.HudBarsFabric;
import dev.muon.combat_attributes.compat.AppleSkinHeartTooltip;
import dev.muon.combat_attributes.compat.AppleSkinIntegrationFabric;
import dev.muon.combat_attributes.compat.DynamicTooltipsIntegration;
import dev.muon.combat_attributes.network.ResourceNetworkFabricClient;
import dev.muon.combat_attributes.platform.Services;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client-only entrypoint. Hosts integrations that touch client-only mods (currently
 * Dynamic Tooltips), so the dedicated-server jar never resolves their classes.
 * Dedicated servers still load {@link CombatAttributesFabric} for attribute registration
 * and the diminishing math.
 *
 * <p>Dynamic Tooltips is a soft dep; without it, our percent-flavored attributes display
 * as flat decimals (e.g. {@code +0.1} instead of {@code +10%}). The {@code isModLoaded}
 * gate keeps the JVM from class-loading {@link DynamicTooltipsIntegration} (and through it,
 * DT's API class) when DT isn't on the classpath.
 *
 * <p>HUD bar registration ({@link HudBarsFabric}) lives here too because Fabric API's
 * {@code HudElementRegistry} is client-only. Gating the call behind
 * {@code ClientModInitializer} matches the wider convention that loader registration
 * happens once at startup, before the registry freezes.
 */
public class CombatAttributesFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        if (Services.PLATFORM.isModLoaded("dynamictooltips")) {
            DynamicTooltipsIntegration.init();
        }
        if (Services.PLATFORM.isModLoaded(AppleSkinHeartTooltip.MOD_ID)) {
            AppleSkinIntegrationFabric.init();
        }
        HudBarsFabric.initClient();
        ResourceNetworkFabricClient.registerClient();
    }
}
