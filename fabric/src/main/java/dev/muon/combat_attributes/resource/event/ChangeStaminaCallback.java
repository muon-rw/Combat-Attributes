package dev.muon.combat_attributes.resource.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;

/**
 * Fabric callback fired whenever a player's stamina value is about to change —
 * gameplay consumption, passive regen, and "current pulled down to max"
 * reconciliations all invoke this. Listeners can mutate the new value or
 * cancel the change outright.
 *
 * <p>Fired only on the logical server, before the attachment write. The new
 * value is re-clamped into {@code [0, getMaxStamina]} after listeners run, so
 * a listener cannot push the pool above max via this hook.
 *
 * <p>The callback threads the new value through the listener chain — each
 * listener receives the value the previous listener returned. Return
 * {@code oldValue} to cancel the change (no write happens). The first listener
 * that cancels short-circuits the chain so a later listener cannot silently
 * un-cancel by returning a different value, mirroring NeoForge's
 * default-skip-after-cancel behavior.
 */
@FunctionalInterface
public interface ChangeStaminaCallback {

    Event<ChangeStaminaCallback> EVENT = EventFactory.createArrayBacked(
            ChangeStaminaCallback.class,
            listeners -> (player, oldValue, newValue) -> {
                float current = newValue;
                for (ChangeStaminaCallback listener : listeners) {
                    current = listener.onChangeStamina(player, oldValue, current);
                    if (current == oldValue) return oldValue;
                }
                return current;
            }
    );

    /**
     * @param newValue the value the next listener (or the resource store) will see;
     *                 the previous listener may have mutated it
     * @return the value to pass on; return {@code oldValue} to cancel the change
     */
    float onChangeStamina(Player player, float oldValue, float newValue);
}
