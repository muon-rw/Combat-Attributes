package dev.muon.combat_attributes.platform;

import dev.muon.combat_attributes.resource.event.ChangeManaCallback;
import dev.muon.combat_attributes.resource.event.ChangeStaminaCallback;
import dev.muon.combat_attributes.platform.services.IPlatformHelper;
import dev.muon.combat_attributes.resource.PlayerResourceStore;
import dev.muon.combat_attributes.resource.PlayerResourceStoreFabric;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

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

    @Override
    public float fireChangeStamina(Player player, float oldValue, float newValue) {
        if (player.level().isClientSide()) return newValue;
        return ChangeStaminaCallback.EVENT.invoker().onChangeStamina(player, oldValue, newValue);
    }

    @Override
    public float fireChangeMana(Player player, float oldValue, float newValue) {
        if (player.level().isClientSide()) return newValue;
        return ChangeManaCallback.EVENT.invoker().onChangeMana(player, oldValue, newValue);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToPlayersTrackingEntity(Entity subject, CustomPacketPayload payload) {
        for (ServerPlayer viewer : PlayerLookup.tracking(subject)) {
            ServerPlayNetworking.send(viewer, payload);
        }
    }
}
