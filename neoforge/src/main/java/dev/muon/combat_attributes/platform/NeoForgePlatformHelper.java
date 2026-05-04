package dev.muon.combat_attributes.platform;

import dev.muon.combat_attributes.resource.event.ChangeManaEvent;
import dev.muon.combat_attributes.resource.event.ChangeStaminaEvent;
import dev.muon.combat_attributes.platform.services.IPlatformHelper;
import dev.muon.combat_attributes.resource.PlayerResourceStore;
import dev.muon.combat_attributes.resource.PlayerResourceStoreNeoforge;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;

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

    @Override
    public float fireChangeStamina(Player player, float oldValue, float newValue) {
        if (player.level().isClientSide()) return newValue;
        ChangeStaminaEvent event = new ChangeStaminaEvent(player, oldValue, newValue);
        NeoForge.EVENT_BUS.post(event);
        return event.isCanceled() ? oldValue : event.getNewValue();
    }

    @Override
    public float fireChangeMana(Player player, float oldValue, float newValue) {
        if (player.level().isClientSide()) return newValue;
        ChangeManaEvent event = new ChangeManaEvent(player, oldValue, newValue);
        NeoForge.EVENT_BUS.post(event);
        return event.isCanceled() ? oldValue : event.getNewValue();
    }
}
