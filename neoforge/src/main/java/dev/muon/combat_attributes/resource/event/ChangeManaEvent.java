package dev.muon.combat_attributes.resource.event;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Posted on {@link NeoForge#EVENT_BUS} whenever a player's mana value is about
 * to change — gameplay consumption, passive regen, and "current pulled down to
 * max" reconciliations all fire this. Listeners can mutate the new value or
 * cancel the change outright.
 *
 * <p>Fired only on the logical server, before the attachment write. The new
 * value is re-clamped into {@code [0, getMaxMana]} after listeners run, so a
 * listener cannot push the pool above max via this hook.
 *
 * <p>{@link #setCanceled(boolean) Cancelling} skips the underlying store
 * write entirely; the dispatcher returns {@link #getOldValue()} regardless of
 * any {@link #setNewValue(float)} calls. Listeners registered with
 * {@code receiveCanceled = true} that read {@link #getNewValue()} after a peer
 * has cancelled will see the unmodified pre-cancel target — cancellation does
 * not reset it.
 */
public final class ChangeManaEvent extends Event implements ICancellableEvent {

    private final Player player;
    private final float oldValue;
    private float newValue;

    public ChangeManaEvent(Player player, float oldValue, float newValue) {
        this.player = player;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Player getPlayer() {
        return player;
    }

    public float getOldValue() {
        return oldValue;
    }

    public float getNewValue() {
        return newValue;
    }

    public void setNewValue(float newValue) {
        this.newValue = newValue;
    }
}
