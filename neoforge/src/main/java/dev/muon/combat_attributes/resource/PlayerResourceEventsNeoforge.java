package dev.muon.combat_attributes.resource;

import dev.muon.combat_attributes.CombatAttributes;
import dev.muon.combat_attributes.resource.event.ChangeStaminaEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * NeoForge server-side hooks for stamina/mana regen, the per-tick stamina
 * consumers, and the {@code stamina_cost} multiplier listener.
 *
 * <p>We use {@code PlayerTickEvent.Post} so we read the "end of tick" view of
 * the player's resources, after every system has mutated state. Same cadence as
 * the Fabric {@code END_SERVER_TICK} path.
 *
 * <p>The {@link ChangeStaminaEvent} listener centralises the {@code stamina_cost}
 * multiplier so every drain (first-party consumers, ticker drains, third-party
 * {@code setStamina} callers) picks it up through one shared listener.
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

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        // Seed the tracking client with the target player's pool to extrapolate from.
        if (event.getEntity() instanceof ServerPlayer viewer) {
            ResourceSync.sendAnchorTo(viewer, event.getTarget());
        }
    }
}
