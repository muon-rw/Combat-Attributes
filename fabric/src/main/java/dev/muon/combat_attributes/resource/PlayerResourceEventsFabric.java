package dev.muon.combat_attributes.resource;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fabric server-side hooks for stamina/mana regen. Iterating online players
 * in {@code END_SERVER_TICK} mirrors the cadence Dynamic Difficulty uses for
 * its per-player tick work — it runs once per tick after entities have moved,
 * so resource updates land in the snapshot the next client packet sees.
 */
public final class PlayerResourceEventsFabric {

    private PlayerResourceEventsFabric() {}

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                PlayerResourceTicker.onPlayerTick(player);
            }
        });
    }
}
