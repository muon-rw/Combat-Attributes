package dev.muon.combat_attributes.resource;

import dev.muon.combat_attributes.config.Configs;
import net.minecraft.world.entity.player.Player;

/**
 * Shared per-break stamina drain hook, called from each loader's native pre-break
 * event ({@code BreakBlockEvent} on NeoForge, {@code PlayerBlockBreakEvents.BEFORE}
 * on Fabric). Avoids a common mixin on {@code ServerPlayerGameMode#destroyBlock}.
 *
 * <p>Cost is flat per block, mirroring vanilla's {@code Player.causeFoodExhaustion(0.005F)}
 * from {@code Block#playerDestroy}. The post-break regen pause is armed automatically
 * by {@link PlayerResources#setStamina} (the universal {@code staminaDrainRegenDelay}
 * handler).
 *
 * <p>Unlike the attack handler, this does <em>not</em> cancel the action when the
 * player is exhausted: the exhausted-break penalty is a destroy-speed multiplier in
 * {@code PlayerDestroySpeedMixin} ({@code exhaustedBreakSpeedMultiplier}), like how
 * vanilla's {@code MINING_FATIGUE} slows mining without blocking it. No-op on the
 * client, drain-only side effect on the server.
 */
public final class BlockBreakStaminaHandler {

    private BlockBreakStaminaHandler() {}

    public static void onBreakAttempt(Player player) {
        float cost = Configs.GENERAL.blockBreakStaminaCost.get().floatValue();
        if (cost <= 0.0F) return;
        if (player.level().isClientSide()) return;
        PlayerResources.trySpendStamina(player, cost);
    }
}
