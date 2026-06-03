package dev.muon.combat_attributes.resource;

import net.minecraft.world.entity.player.Player;

public final class PlayerResourceStoreNeoforge implements PlayerResourceStore {

    @Override
    public PlayerResourceData get(Player player) {
        // getExistingDataOrNull, not getData: getData would store a DEFAULT record on a miss,
        // flipping has() true for a not-yet-anchored remote player and defeating the client sync gate.
        // This keeps get() a pure read, matching Fabric's getAttached.
        PlayerResourceData data = player.getExistingDataOrNull(PlayerResourceAttachmentNeoforge.RESOURCES);
        return data != null ? data : PlayerResourceData.DEFAULT;
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
