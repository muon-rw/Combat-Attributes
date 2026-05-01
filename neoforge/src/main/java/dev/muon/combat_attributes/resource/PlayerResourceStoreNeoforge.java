package dev.muon.combat_attributes.resource;

import net.minecraft.world.entity.player.Player;

public final class PlayerResourceStoreNeoforge implements PlayerResourceStore {

    @Override
    public PlayerResourceData get(Player player) {
        return player.getData(PlayerResourceAttachmentNeoforge.RESOURCES);
    }

    @Override
    public void set(Player player, PlayerResourceData data) {
        player.setData(PlayerResourceAttachmentNeoforge.RESOURCES, data);
    }

    @Override
    public boolean has(Player player) {
        return player.getExistingDataOrNull(PlayerResourceAttachmentNeoforge.RESOURCES) != null;
    }
}
