package dev.muon.combat_attributes.resource;

import dev.muon.combat_attributes.attribute.ModAttributes;
import dev.muon.combat_attributes.config.Configs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ProjectileWeaponItem;

/**
 * Per-tick stamina + mana regeneration plus the flag-held stamina consumers (sprint, swim,
 * elytra). Runs server-authoritatively; the attachment update auto-syncs to the owning client.
 * Regen attributes are per-second, spread across 20 ticks.
 */
public final class PlayerResourceTicker {

    private static final float SECONDS_PER_TICK = 1.0F / 20.0F;

    private PlayerResourceTicker() {}

    public static void onPlayerTick(ServerPlayer player) {
        applyContinuousConsumers(player);
        PlayerResourceData data = PlayerResources.get(player);
        PlayerResources.writeIfChanged(player, data, computeRegenTick(player, data));
        // Periodic drift-insurance re-anchor, staggered by tickCount; also seeds clients that missed an earlier anchor.
        if (player.tickCount % ResourceSync.KEEPALIVE_TICKS == 0) {
            ResourceSync.broadcastAnchor(player);
        }
    }

    private static void applyContinuousConsumers(ServerPlayer player) {
        boolean sprinting = player.isSprinting();
        boolean swimming  = player.isSwimming() && player.isUnderWater();
        boolean gliding   = player.isFallFlying();
        if (!sprinting && !swimming && !gliding) return;

        float cost = 0.0F;
        if (sprinting) cost += Configs.GENERAL.sprintStaminaCost.get().floatValue();
        if (swimming)  cost += Configs.GENERAL.swimStaminaCost.get().floatValue();
        if (gliding)   cost += Configs.GENERAL.elytraStaminaCost.get().floatValue();

        if (PlayerResources.trySpendStamina(player, cost)) return;

        // Lockout: force the state flags off so the player has to re-press to retry
        // once recovery completes, rather than instantly re-arming intent each tick.
        if (sprinting) player.setSprinting(false);
        if (swimming)  player.setSwimming(false);
        if (gliding)   player.stopFallFlying();
    }

    /**
     * One pure tick of regen + delay decrement. Shared by the server ticker and the client
     * {@link ClientResourceExtrapolator} so both advance an un-spent pool identically; excludes
     * drains, which reach the client only through a fresh anchor.
     */
    public static PlayerResourceData computeRegenTick(Player player, PlayerResourceData data) {
        float maxStamina = PlayerResources.getMaxStamina(player);
        float maxMana    = PlayerResources.getMaxMana(player);
        int  delay       = data.staminaRegenDelayTicks();

        int nextDelay = Math.max(0, delay - 1);

        float nextStamina = data.stamina() > maxStamina ? maxStamina : data.stamina();
        if (delay == 0 && nextStamina < maxStamina && !shouldPauseStaminaRegen(player)) {
            float staminaRegen = (float) ModAttributes.valueOrDefault(player, ModAttributes.staminaRegen());
            nextStamina = Math.min(nextStamina + staminaRegen * SECONDS_PER_TICK, maxStamina);
        }

        float manaRegen = (float) ModAttributes.valueOrDefault(player, ModAttributes.manaRegen());
        float nextMana  = data.mana() > maxMana ? maxMana : data.mana();
        nextMana = Math.min(nextMana + manaRegen * SECONDS_PER_TICK, maxMana);

        return new PlayerResourceData(nextStamina, nextMana, nextDelay);
    }

    private static boolean shouldPauseStaminaRegen(Player player) {
        if (Configs.GENERAL == null) {
            return false; // config not loaded yet; match the server's "don't pause" so the shared step stays in lockstep
        }
        return (Configs.GENERAL.rangedDrawPausesStaminaRegen.get()
                    && player.isUsingItem()
                    && player.getUseItem().getItem() instanceof ProjectileWeaponItem)
                || (Configs.GENERAL.underwaterPausesStaminaRegen.get() && player.isUnderWater());
    }
}
