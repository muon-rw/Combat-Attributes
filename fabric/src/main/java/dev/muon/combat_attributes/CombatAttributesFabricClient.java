package dev.muon.combat_attributes;

import dev.muon.combat_attributes.client.HudBarsFabric;
import dev.muon.combat_attributes.compat.AppleSkinHeartTooltip;
import dev.muon.combat_attributes.compat.AppleSkinIntegrationFabric;
import dev.muon.combat_attributes.compat.DynamicTooltipsIntegration;
import dev.muon.combat_attributes.network.ResourceNetworkFabricClient;
import dev.muon.combat_attributes.platform.Services;
import net.fabricmc.api.ClientModInitializer;

public class CombatAttributesFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Gate keeps the JVM from class-loading DynamicTooltipsIntegration, and through it DT's API, when DT is absent.
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
