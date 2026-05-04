package dev.muon.combat_attributes.platform.services;

import dev.muon.combat_attributes.resource.PlayerResourceStore;
import net.minecraft.world.entity.player.Player;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }

    /**
     * Loader-specific accessor for the player's stamina/mana attachment.
     * Both loaders back this with an auto-syncing, persistent attachment.
     */
    PlayerResourceStore getPlayerResourceStore();

    /**
     * Posts the loader's {@code ChangeStaminaEvent} (NeoForge) or invokes the
     * {@code ChangeStaminaCallback} chain (Fabric) for a pending stamina write.
     *
     * <p>Listeners may set a different new value or cancel the change. Cancellation
     * is reported by returning {@code oldValue}, so callers can simply re-compare
     * the result against the prior value to decide whether to write.
     *
     * <p>Fired only on the logical server. The returned value is not re-clamped
     * here; callers (e.g. {@link dev.muon.combat_attributes.resource.PlayerResources})
     * are responsible for clamping into the current {@code [0, max]} range so a
     * listener cannot push the pool above max via this hook.
     */
    float fireChangeStamina(Player player, float oldValue, float newValue);

    /** Mana counterpart to {@link #fireChangeStamina(Player, float, float)}. */
    float fireChangeMana(Player player, float oldValue, float newValue);
}
