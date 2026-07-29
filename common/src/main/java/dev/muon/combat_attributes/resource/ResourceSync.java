package dev.muon.combat_attributes.resource;

import dev.muon.combat_attributes.network.ResourceAnchorPayload;
import dev.muon.combat_attributes.platform.Services;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Server &rarr; tracker broadcasts of resource anchors. Fired on pool
 * perturbations (spends/drains, via {@link PlayerResources}) plus a periodic
 * keepalive (via {@link PlayerResourceTicker}), never on the per-tick regen,
 * which the client reconstructs itself in {@link ClientResourceExtrapolator}.
 * The owning client is excluded: it gets the value over the attachment sync.
 */
public final class ResourceSync {

    /**
     * Keepalive re-anchor cadence (ticks). Regen is reproduced client-side, so this is only drift
     * insurance against dropped packets / client tick hitches; it doesn't carry the regen itself.
     */
    public static final int KEEPALIVE_TICKS = 600;

    private ResourceSync() {}

    /** Broadcast {@code subject}'s current pool to every client tracking them. No-op off the server. */
    public static void broadcastAnchor(Player subject) {
        if (subject instanceof ServerPlayer player) {
            Services.PLATFORM.sendToPlayersTrackingEntity(player,
                    new ResourceAnchorPayload(player.getId(), PlayerResources.get(player)));
        }
    }

    /** Seed a newly-tracking {@code viewer} with {@code subject}'s current pool. */
    public static void sendAnchorTo(ServerPlayer viewer, Entity subject) {
        if (subject instanceof ServerPlayer player) {
            Services.PLATFORM.sendToPlayer(viewer,
                    new ResourceAnchorPayload(player.getId(), PlayerResources.get(player)));
        }
    }
}
