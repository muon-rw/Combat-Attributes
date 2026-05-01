package dev.muon.combat_attributes.resource;

import net.minecraft.world.entity.player.Player;

public final class PlayerResourceStoreFabric implements PlayerResourceStore {

    @Override
    public PlayerResourceData get(Player player) {
        PlayerResourceData data = player.getAttached(PlayerResourceAttachmentFabric.RESOURCES);
        return data != null ? data : PlayerResourceData.DEFAULT;
    }

    @Override
    public void set(Player player, PlayerResourceData data) {
        player.setAttached(PlayerResourceAttachmentFabric.RESOURCES, data);
    }

    @Override
    public boolean has(Player player) {
        return player.hasAttached(PlayerResourceAttachmentFabric.RESOURCES);
    }
}
