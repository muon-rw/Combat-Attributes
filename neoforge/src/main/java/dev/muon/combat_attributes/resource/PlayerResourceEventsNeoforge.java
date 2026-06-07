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

@EventBusSubscriber(modid = CombatAttributes.MOD_ID)
public final class PlayerResourceEventsNeoforge {

    private PlayerResourceEventsNeoforge() {}

    // Post reads the end-of-tick resource view, after every system has mutated state.
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
        BlockBreakStaminaHandler.applyBreakCost(event.getPlayer());
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        // Seed the tracking client with the target player's pool to extrapolate from.
        if (event.getEntity() instanceof ServerPlayer viewer) {
            ResourceSync.sendAnchorTo(viewer, event.getTarget());
        }
    }
}
