package dev.muon.combat_attributes.platform;

import dev.muon.combat_attributes.platform.services.IPlatformHelper;
import dev.muon.combat_attributes.resource.PlayerResourceStore;
import dev.muon.combat_attributes.resource.PlayerResourceStoreNeoforge;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

public class NeoForgePlatformHelper implements IPlatformHelper {

    private static final PlayerResourceStore RESOURCE_STORE = new PlayerResourceStoreNeoforge();

    @Override
    public String getPlatformName() {

        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.getCurrent().isProduction();
    }

    @Override
    public PlayerResourceStore getPlayerResourceStore() {
        return RESOURCE_STORE;
    }
}
