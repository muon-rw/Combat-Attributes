package dev.muon.combat_attributes.resource;

import dev.muon.combat_attributes.platform.Services;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side reconstruction of <em>other</em> players' stamina/mana between the
 * sparse anchor packets ({@link dev.muon.combat_attributes.network.ResourceAnchorPayload}).
 * The server doesn't stream per-tick regen; this advances each tracked player's
 * pool locally with the same step the server runs
 * ({@link PlayerResourceTicker#computeRegenTick}), so a player below max appears
 * to refill smoothly without a packet every tick. A fresh anchor (spend/drain,
 * start-tracking, or keepalive) re-seats the value.
 *
 * <p>The owning client isn't extrapolated here (it has the attachment sync), so
 * {@link #tick} skips the local player.
 *
 * <p>An anchor can arrive before its entity has spawned on the client; those get
 * stashed in {@link #PENDING} and applied once the entity appears (with a timeout
 * so a never-arriving entity can't leak), mirroring Dynamic Difficulty's
 * {@code SyncLevelingData} race handling.
 */
public final class ClientResourceExtrapolator {

    private static final int PENDING_TIMEOUT_TICKS = 100;
    private static final int MAX_PENDING = 1024;

    private static final Map<Integer, Pending> PENDING = new ConcurrentHashMap<>();

    private record Pending(PlayerResourceData data, long tickReceived) {}

    private ClientResourceExtrapolator() {}

    public static void receiveAnchor(Level level, int entityId, PlayerResourceData data) {
        if (level == null) {
            return;
        }
        if (level.getEntity(entityId) instanceof Player player) {
            Services.PLATFORM.getPlayerResourceStore().set(player, data);
        } else if (PENDING.size() < MAX_PENDING) {
            PENDING.put(entityId, new Pending(data, level.getGameTime()));
        }
    }

    public static void tick(Level level, Player self) {
        if (level == null) {
            return;
        }
        advanceTrackedPlayers(level, self);
        // Seat pending anchors AFTER advancing, so a player seated this tick isn't also advanced this tick
        // (that would overshoot the anchor by one regen step); it starts advancing next tick instead.
        applyPending(level);
    }

    private static void advanceTrackedPlayers(Level level, Player self) {
        PlayerResourceStore store = Services.PLATFORM.getPlayerResourceStore();
        for (Player player : level.players()) {
            if (player == self || !store.has(player)) {
                continue;
            }
            store.set(player, PlayerResourceTicker.computeRegenTick(player, store.get(player)));
        }
    }

    public static void clear() {
        PENDING.clear();
    }

    private static void applyPending(Level level) {
        if (PENDING.isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        PlayerResourceStore store = Services.PLATFORM.getPlayerResourceStore();
        Iterator<Map.Entry<Integer, Pending>> it = PENDING.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Pending> entry = it.next();
            if (now - entry.getValue().tickReceived() > PENDING_TIMEOUT_TICKS) {
                it.remove();
            } else if (level.getEntity(entry.getKey()) instanceof Player player) {
                store.set(player, entry.getValue().data());
                it.remove();
            }
        }
    }
}
