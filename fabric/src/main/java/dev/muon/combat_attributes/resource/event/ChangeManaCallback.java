package dev.muon.combat_attributes.resource.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;

/**
 * Fired whenever a player's mana is about to change (consumption, regen, or a
 * clamp-down to max). Listeners can mutate the new value or cancel the change.
 *
 * <p>Server-side only, before the attachment write. The result is re-clamped into
 * {@code [0, getMaxMana]} after listeners run, so a listener can't push past max.
 *
 * <p>Listeners are chained: each sees the value the previous one returned. Return
 * {@code oldValue} to cancel; the first cancel short-circuits the rest, so a later
 * listener can't un-cancel. Mirrors NeoForge's default-skip-after-cancel.
 */
@FunctionalInterface
public interface ChangeManaCallback {

    Event<ChangeManaCallback> EVENT = EventFactory.createArrayBacked(
            ChangeManaCallback.class,
            listeners -> (player, oldValue, newValue) -> {
                float current = newValue;
                for (ChangeManaCallback listener : listeners) {
                    current = listener.onChangeMana(player, oldValue, current);
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
    float onChangeMana(Player player, float oldValue, float newValue);
}
