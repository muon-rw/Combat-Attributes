package dev.muon.combat_attributes.resource;

import dev.muon.combat_attributes.resource.event.ChangeStaminaCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public final class PlayerResourceEventsFabric {

    private PlayerResourceEventsFabric() {}

    public static void init() {
        ChangeStaminaCallback.EVENT.register((player, oldValue, newValue) ->
                StaminaCostListener.applyCostMultiplier(player, oldValue, newValue));

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
                AttackStaminaHandler.shouldCancelAttack(player) ? InteractionResult.FAIL : InteractionResult.PASS);

        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            BlockBreakStaminaHandler.applyBreakCost(player);
            return true;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                PlayerResourceTicker.onPlayerTick(player);
            }
        });

        // Seed the pool at tracking-start so over-head bars can extrapolate before the next spend.
        EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) ->
                ResourceSync.sendAnchorTo(player, trackedEntity));
    }
}
