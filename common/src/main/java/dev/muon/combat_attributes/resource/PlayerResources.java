package dev.muon.combat_attributes.resource;

import dev.muon.combat_attributes.attribute.ModAttributes;
import dev.muon.combat_attributes.config.Configs;
import dev.muon.combat_attributes.platform.Services;
import net.minecraft.world.entity.player.Player;

/**
 * Static facade for reading + writing the player's stamina/mana state.
 *
 * <p>Routes through {@link Services#PLATFORM} so common code stays loader-agnostic.
 * Both loaders' attachments auto-sync to the owning client on write, so callers
 * don't dispatch packets manually.
 *
 * <p>Read methods clamp into {@code [0, max]} on the way out so callers never see
 * a stale "current > max" after the player's max attribute dropped.
 *
 * <p>Every write path dispatches the loader-native {@code ChangeStaminaEvent} /
 * {@code ChangeManaEvent} via {@link Services#PLATFORM} before committing, so
 * listeners can mutate or cancel the change. Listeners get the raw caller
 * argument, which can fall outside {@code [0, max]} (a consumer draining more
 * stamina than the player has passes a negative). The returned value is
 * re-clamped into {@code [0, max]} after listeners run, so they can't push the
 * pool out of range through this hook; a no-op result (listener returned
 * {@code oldValue}) skips the write.
 *
 * <p>When a write brings stamina from positive down to exactly zero, the
 * persisted {@code staminaRegenDelayTicks} is set from
 * {@link dev.muon.combat_attributes.config.ConfigGeneral#staminaEmptyRegenDelay}
 * so the regen ticker pauses recovery during the exhaustion window. Mana writes
 * never touch this field.
 */
public final class PlayerResources {

    private PlayerResources() {}

    public static PlayerResourceData get(Player player) {
        return Services.PLATFORM.getPlayerResourceStore().get(player);
    }

    /**
     * Whether a synced/stored resource record exists for {@code player}. On the client this distinguishes a player
     * whose pool has arrived from one still reading {@link PlayerResourceData#DEFAULT}; callers that route by
     * stamina/mana (over-head bars, resource orbs) should treat "no record yet" as "unknown", not "empty".
     */
    public static boolean hasResourceData(Player player) {
        return Services.PLATFORM.getPlayerResourceStore().has(player);
    }

    public static float getStamina(Player player) {
        return clamp(get(player).stamina(), getMaxStamina(player));
    }

    public static float getMana(Player player) {
        return clamp(get(player).mana(), getMaxMana(player));
    }

    public static float getMaxStamina(Player player) {
        return (float) ModAttributes.valueOrDefault(player, ModAttributes.maxStamina());
    }

    public static float getMaxMana(Player player) {
        return (float) ModAttributes.valueOrDefault(player, ModAttributes.maxMana());
    }

    /**
     * Whether the player has any stamina available to spend right now. While
     * stamina sits at zero (the post-exhaustion lockout window), consumers
     * should refuse to perform their action rather than no-op-drain.
     */
    public static boolean canSpendStamina(Player player) {
        return getStamina(player) > 0.0F;
    }

    /** Mana counterpart to {@link #canSpendStamina(Player)}. No lockout, just a {@code > 0} check. */
    public static boolean canSpendMana(Player player) {
        return getMana(player) > 0.0F;
    }

    /**
     * Atomically gate-and-spend the configured cost. Returns {@code true} if the player
     * had any stamina (the spend went through, possibly taking them to zero and arming
     * the lockout) or {@code false} if stamina was already exhausted (no spend happened
     * and the caller should block its action). Costs of {@code <= 0} are a trivial
     * success, handy for "feature disabled" config values without a branch at every
     * call site.
     *
     * <p>Typical use:
     * <pre>{@code
     *   if (!PlayerResources.trySpendStamina(player, cost)) {
     *       blockAction();
     *       return;
     *   }
     * }</pre>
     */
    public static boolean trySpendStamina(Player player, float cost) {
        if (cost <= 0.0F) return true;
        float currentStamina = getStamina(player);
        if (currentStamina <= 0.0F) return false;
        setStamina(player, currentStamina - cost);
        return true;
    }

    /** Mana counterpart to {@link #trySpendStamina(Player, float)}. */
    public static boolean trySpendMana(Player player, float cost) {
        if (cost <= 0.0F) return true;
        float currentMana = getMana(player);
        if (currentMana <= 0.0F) return false;
        setMana(player, currentMana - cost);
        return true;
    }

    public static void setStamina(Player player, float stamina) {
        PlayerResourceData current = get(player);
        if (stamina == current.stamina()) return;
        boolean intentToDeplete = stamina <= 0.0F && current.stamina() > 0.0F;
        float resolved = clamp(Services.PLATFORM.fireChangeStamina(player, current.stamina(), stamina), getMaxStamina(player));
        boolean drained = resolved < current.stamina();
        int delay = nextStaminaRegenDelay(current.staminaRegenDelayTicks(), intentToDeplete, drained);
        PlayerResourceData finalData = new PlayerResourceData(resolved, current.mana(), delay);
        if (finalData.equals(current)) return;
        Services.PLATFORM.getPlayerResourceStore().set(player, finalData);
        // A spend/drain is a perturbation the client can't predict; re-anchor its trackers.
        ResourceSync.broadcastAnchor(player);
    }

    public static void setMana(Player player, float mana) {
        PlayerResourceData current = get(player);
        if (mana == current.mana()) return;
        float resolved = clamp(Services.PLATFORM.fireChangeMana(player, current.mana(), mana), getMaxMana(player));
        if (resolved == current.mana()) return;
        Services.PLATFORM.getPlayerResourceStore().set(player, current.withMana(resolved));
        // A spend is a perturbation the client can't predict; re-anchor its trackers.
        ResourceSync.broadcastAnchor(player);
    }

    /**
     * Package-private write path for callers (i.e. the regen ticker) that already hold the
     * prior {@link PlayerResourceData} and have produced clamped values themselves. Skips
     * the re-fetch a public setter would perform, but still dispatches the per-resource
     * change events and re-clamps any listener-mutated value.
     *
     * <p>Trusts the caller's {@code next.staminaRegenDelayTicks()} as authoritative;
     * lockout arming is the job of the public depletion paths
     * ({@link #setStamina}, {@link #trySpendStamina}). The ticker decrements the timer
     * through this method without re-arming it.
     */
    static void writeIfChanged(Player player, PlayerResourceData previous, PlayerResourceData next) {
        if (next.equals(previous)) return;
        float s = next.stamina();
        float m = next.mana();
        if (s != previous.stamina()) {
            s = clamp(Services.PLATFORM.fireChangeStamina(player, previous.stamina(), s), getMaxStamina(player));
        }
        if (m != previous.mana()) {
            m = clamp(Services.PLATFORM.fireChangeMana(player, previous.mana(), m), getMaxMana(player));
        }
        PlayerResourceData finalData = new PlayerResourceData(s, m, next.staminaRegenDelayTicks());
        if (finalData.equals(previous)) return;
        Services.PLATFORM.getPlayerResourceStore().set(player, finalData);
    }

    /**
     * Composes the next regen-delay value from the existing timer plus two arming
     * sources. Both land via {@code max()} so neither shortens the other: draining
     * to zero in one shot raises the timer to the heavier exhaustion lockout, not
     * the lighter universal drain delay.
     *
     * <ul>
     *   <li><b>{@code armDrainPause}</b>: every successful drain (post-listener
     *       resolved value lower than current) arms {@code staminaDrainRegenDelay}.
     *       Souls-like recovery window between expenditure and regen.</li>
     *   <li><b>{@code armLockout}</b>: raw caller intent of "deplete to zero or
     *       below" arms {@code staminaEmptyRegenDelay}. Uses raw intent rather than
     *       post-listener value because the {@code stamina_cost} multiplier can keep
     *       stamina from landing at exactly zero; without this, players in that
     *       asymptotic regime would never trigger exhaustion.</li>
     * </ul>
     */
    private static int nextStaminaRegenDelay(int proposedDelay, boolean armLockout, boolean armDrainPause) {
        int target = proposedDelay;
        if (armDrainPause) target = Math.max(target, drainPauseTicks());
        if (armLockout)    target = Math.max(target, lockoutTicks());
        return target;
    }

    private static int lockoutTicks() {
        return secondsToTicks(Configs.GENERAL.staminaEmptyRegenDelay.get());
    }

    private static int drainPauseTicks() {
        return secondsToTicks(Configs.GENERAL.staminaDrainRegenDelay.get());
    }

    private static int secondsToTicks(double seconds) {
        if (seconds <= 0.0) return 0;
        return (int) Math.round(seconds * 20.0);
    }

    private static float clamp(float v, float max) {
        if (max <= 0.0F) return 0.0F;
        if (v < 0.0F) return 0.0F;
        if (v > max) return max;
        return v;
    }
}
