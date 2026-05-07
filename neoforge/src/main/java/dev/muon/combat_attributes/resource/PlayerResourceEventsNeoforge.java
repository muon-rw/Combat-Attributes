package dev.muon.combat_attributes.resource;

import dev.muon.combat_attributes.CombatAttributes;
import dev.muon.combat_attributes.resource.event.ChangeStaminaEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * NeoForge server-side hooks for stamina/mana regen + the per-tick stamina
 * consumers, and the {@code stamina_cost} multiplier listener.
 *
 * <p>The {@code PlayerTickEvent.Post} variant fires after every system has
 * had a chance to mutate state this tick, so we see the canonical
 * "end of tick" view of the player's resources before writing — matches the
 * cadence of the Fabric {@code END_SERVER_TICK} path.
 *
 * <p>The {@link ChangeStaminaEvent} listener centralises the
 * {@code stamina_cost} attribute multiplier so every drain — first-party
 * consumers, ticker drains, and any third-party {@code setStamina} caller —
 * picks up the multiplier through one shared listener.
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

    @SubscribeEvent
    public static void onChangeStamina(ChangeStaminaEvent event) {
        float adjusted = StaminaCostListener.applyCostMultiplier(
                event.getPlayer(), event.getOldValue(), event.getNewValue());
        event.setNewValue(adjusted);
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (AttackStaminaHandler.shouldCancelAttack(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBreakBlock(BreakBlockEvent event) {
        BlockBreakStaminaHandler.onBreakAttempt(event.getPlayer());
    }
}
