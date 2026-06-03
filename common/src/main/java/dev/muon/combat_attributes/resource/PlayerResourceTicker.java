package dev.muon.combat_attributes.resource;

import dev.muon.combat_attributes.attribute.ModAttributes;
import dev.muon.combat_attributes.config.Configs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ProjectileWeaponItem;

/**
 * Per-tick stamina + mana regeneration plus the per-tick stamina consumers
 * that fire while a state flag is held (sprint, swim, elytra). Runs
 * server-authoritatively; the resulting attachment update auto-syncs to the
 * owning client.
 *
 * <p>Regen attribute units are <em>per second</em>, spread across 20 ticks so
 * fractional rates (e.g. 0.5/sec) accumulate predictably. Mana and stamina
 * regen-or-skip decisions fold into a single
 * {@link PlayerResources#writeIfChanged} so a typical tick produces at most
 * one sync packet for the regen path.
 *
 * <p>Per-tick drains are applied first via {@link PlayerResources#setStamina},
 * which fires {@code ChangeStaminaEvent} / {@code ChangeStaminaCallback} like
 * any external consumer would, so the {@code stamina_cost} attribute multiplier
 * is picked up by the same listener that handles all other drain paths.
 *
 * <p>While stamina sits at zero (the post-exhaustion lockout window), the
 * three continuous consumers force-cancel their respective state flags
 * ({@code setSprinting(false)}, {@code setSwimming(false)},
 * {@code stopFallFlying()}) so the player isn't silently re-incurring drain
 * intents during recovery.
 */
public final class PlayerResourceTicker {

    private static final float SECONDS_PER_TICK = 1.0F / 20.0F;

    private PlayerResourceTicker() {}

    public static void onPlayerTick(ServerPlayer player) {
        applyContinuousConsumers(player);
        applyRegenAndDecrementLockout(player);
        // Drains above already re-anchor via setStamina; this is the periodic drift-insurance re-anchor
        // (and seeds clients that may have missed an earlier anchor). Staggered by per-player tickCount.
        if (player.tickCount % ResourceSync.KEEPALIVE_TICKS == 0) {
            ResourceSync.broadcastAnchor(player);
        }
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
     * <p>Stamina regen runs only when both the persisted delay timer
     * ({@code staminaRegenDelayTicks}) is zero AND no live regen-pause condition
     * holds (see {@link #shouldPauseStaminaRegen}). Mana regen is unconditional.
     * Values over the current max are clamped down here so a buff-expire scenario
     * (max dropped after the last write) stops drifting once the ticker notices.
     */
    private static void applyRegenAndDecrementLockout(ServerPlayer player) {
        PlayerResourceData data = PlayerResources.get(player);
        PlayerResources.writeIfChanged(player, data, computeRegenTick(player, data));
    }

    /**
     * One tick of regen + delay decrement, as a pure function of {@code data} plus the player's live state
     * (max/regen attributes, the {@link #shouldPauseStaminaRegen} predicate). Shared by the server ticker and the
     * client-side {@link ClientResourceExtrapolator}, so both sides advance an un-spent pool along the same
     * trajectory and the server needn't stream the regen. Excludes drains, which are server-only events the client
     * learns about through a fresh anchor.
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

    /**
     * Live-state regen-pause predicates evaluated each tick. Composes with the
     * persisted {@code staminaRegenDelayTicks} timer (post-exhaustion lockout,
     * universal post-drain delay) at the call site; this method only answers for
     * the live-state sources (ranged draw, underwater).
     */
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
