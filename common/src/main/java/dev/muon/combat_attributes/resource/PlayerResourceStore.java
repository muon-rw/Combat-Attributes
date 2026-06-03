package dev.muon.combat_attributes.resource;

import net.minecraft.world.entity.player.Player;

/**
 * Loader-abstracted accessor for {@link PlayerResourceData}. Implementations
 * live in the {@code fabric}/{@code neoforge} modules and back this with the
 * loader's attachment system.
 *
 * <p>Mirrors the shape of {@code PlayerLevelStore} in Chronicles Leveling so
 * that the same conventions (default record on read, set replaces wholesale,
 * no partial-field updates) carry over.
 */
public interface PlayerResourceStore {

    /**
     * Returns the player's current data, or {@link PlayerResourceData#DEFAULT} if unset. Must be a pure read:
     * implementations must NOT create/store/sync a record on a miss, so {@link #has(Player)} stays an honest
     * "has a record ever been written/received" probe; the client-side sync gate depends on that.
     */
    PlayerResourceData get(Player player);

    /**
     * Writes the player's data. Persistence and sync to the owning client are
     * handled by the underlying attachment; both loaders auto-sync on
     * {@code setData}/{@code setAttached}.
     */
    void set(Player player, PlayerResourceData data);

    /** Whether the player has a non-default record stored. */
    boolean has(Player player);
}
