package dev.muon.combat_attributes.resource;

import dev.muon.combat_attributes.CombatAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * NeoForge server-side hooks for stamina/mana regen. The {@code Post} variant
 * fires after every system has had a chance to mutate state this tick, so we
 * see the canonical "end of tick" view of the player's resources before
 * writing — matches the cadence of the Fabric {@code END_SERVER_TICK} path.
 */
@EventBusSubscriber(modid = CombatAttributes.MOD_ID)
public final class PlayerResourceEventsNeoforge {

    private PlayerResourceEventsNeoforge() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerResourceTicker.onPlayerTick(player);
        }
    }
}
