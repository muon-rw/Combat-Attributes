package dev.muon.combat_attributes.platform;

import dev.muon.combat_attributes.platform.services.IPlatformHelper;
import dev.muon.combat_attributes.resource.PlayerResourceStore;
import dev.muon.combat_attributes.resource.PlayerResourceStoreFabric;
import net.fabricmc.loader.api.FabricLoader;

public class FabricPlatformHelper implements IPlatformHelper {

    private static final PlayerResourceStore RESOURCE_STORE = new PlayerResourceStoreFabric();

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public PlayerResourceStore getPlayerResourceStore() {
        return RESOURCE_STORE;
    }
}
