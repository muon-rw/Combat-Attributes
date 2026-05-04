package dev.muon.combat_attributes.resource;

import dev.muon.combat_attributes.attribute.ModAttributes;
import dev.muon.combat_attributes.config.Configs;
import net.minecraft.server.level.ServerPlayer;

/**
 * Per-tick stamina + mana regeneration plus the per-tick stamina consumers
 * that fire while a state flag is held — sprint, swim, elytra. Runs
 * server-authoritatively; the resulting attachment update auto-syncs to the
 * owning client.
 *
 * <p>Regen attribute units are <em>per second</em>, distributed across 20
 * ticks so fractional rates (e.g. 0.5/sec) accumulate predictably. Mana and
 * stamina regen-or-skip decisions are folded into a single
 * {@link PlayerResources#writeIfChanged} so a typical tick produces at most
 * one sync packet for the regen path.
 *
 * <p>Per-tick drains are applied first via {@link PlayerResources#setStamina},
 * which fires {@code ChangeStaminaEvent} / {@code ChangeStaminaCallback}
 * exactly like an external consumer would — the {@code stamina_cost}
 * attribute multiplier is therefore picked up by the same listener that
 * handles all other drain paths.
 *
 * <p>While stamina sits at zero (the post-exhaustion lockout window), the
 * three continuous consumers force-cancel their respective state flags
 * ({@code setSprinting(false)}, {@code setSwimming(false)},
 * {@code stopFallFlying()}) so the player is not silently re-incurring drain
 * intents during the recovery period.
 */
public final class PlayerResourceTicker {

    private static final float SECONDS_PER_TICK = 1.0F / 20.0F;

    private PlayerResourceTicker() {}

    public static void onPlayerTick(ServerPlayer player) {
        applyContinuousConsumers(player);
        applyRegenAndDecrementLockout(player);
    }

    /**
     * Drains for sprint / swim / elytra, blocking each via its loader-agnostic
     * state setter once stamina is exhausted. Goes through
     * {@link PlayerResources#trySpendStamina} so the gate-and-drain check is
     * a single call, the {@code stamina_cost} listener applies, and the lockout
     * trigger fires from the central path.
     */
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
     * Combined regen + delay-tick decrement, written through
     * {@link PlayerResources#writeIfChanged} so an idle player at full pools
     * with no lockout running produces zero packets.
     *
     * <p>Stamina regen is gated by the persisted lockout timer
     * ({@code staminaRegenDelayTicks}); mana regen is unconditional. Values
     * over the current max are clamped down here so a buff-expire scenario
     * (max dropped after the last write) stops drifting once the ticker
     * notices.
     */
    private static void applyRegenAndDecrementLockout(ServerPlayer player) {
        PlayerResourceData data = PlayerResources.get(player);
        float maxStamina = PlayerResources.getMaxStamina(player);
        float maxMana    = PlayerResources.getMaxMana(player);
        int  delay       = data.staminaRegenDelayTicks();

        int nextDelay = Math.max(0, delay - 1);

        float nextStamina = data.stamina() > maxStamina ? maxStamina : data.stamina();
        if (delay == 0) {
            float staminaRegen = (float) ModAttributes.valueOrDefault(player, ModAttributes.staminaRegen());
            nextStamina = Math.min(nextStamina + staminaRegen * SECONDS_PER_TICK, maxStamina);
        }

        float manaRegen = (float) ModAttributes.valueOrDefault(player, ModAttributes.manaRegen());
        float nextMana  = data.mana() > maxMana ? maxMana : data.mana();
        nextMana = Math.min(nextMana + manaRegen * SECONDS_PER_TICK, maxMana);

        PlayerResources.writeIfChanged(player, data, new PlayerResourceData(nextStamina, nextMana, nextDelay));
    }
}
