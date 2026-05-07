package dev.muon.combat_attributes.resource;

import dev.muon.combat_attributes.resource.event.ChangeStaminaCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

/**
 * Fabric server-side hooks for stamina/mana regen + the per-tick stamina
 * consumers. Iterating online players in {@code END_SERVER_TICK} mirrors the
 * cadence Dynamic Difficulty uses for its per-player tick work — it runs once
 * per tick after entities have moved, so resource updates land in the
 * snapshot the next client packet sees.
 *
 * <p>Also registers the {@code stamina_cost} multiplier on
 * {@link ChangeStaminaCallback#EVENT} so every drain — first-party consumers,
 * ticker drains, and any third-party {@code setStamina} caller — picks up the
 * attribute through one shared listener rather than each call site reading
 * the multiplier itself.
 */
public final class PlayerResourceEventsFabric {

    private PlayerResourceEventsFabric() {}

    public static void init() {
        ChangeStaminaCallback.EVENT.register((player, oldValue, newValue) ->
                StaminaCostListener.applyCostMultiplier(player, oldValue, newValue));

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
                AttackStaminaHandler.shouldCancelAttack(player) ? InteractionResult.FAIL : InteractionResult.PASS);

        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            BlockBreakStaminaHandler.onBreakAttempt(player);
            return true;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                PlayerResourceTicker.onPlayerTick(player);
            }
        });
    }
}
