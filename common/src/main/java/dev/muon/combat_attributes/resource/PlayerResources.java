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
 * don't need to dispatch packets manually.
 *
 * <p>Read methods always clamp into {@code [0, max]} on the way out so callers
 * never see a stale "current > max" if the player's max attribute dropped after
 * the last write.
 *
 * <p>Every write path dispatches the loader-native {@code ChangeStaminaEvent} /
 * {@code ChangeManaEvent} via {@link Services#PLATFORM} before committing, so
 * listeners can mutate or cancel the change. Listeners receive the raw caller
 * argument — values outside {@code [0, max]} are possible (a consumer that
 * tries to drain more stamina than the player has will pass a negative). The
 * returned value is re-clamped into {@code [0, max]} after listeners run, so
 * listeners cannot push the pool out of range through this hook; a no-op
 * result (listener returned {@code oldValue}) skips the write.
 *
 * <p>Whenever a write brings stamina from positive down to exactly zero, the
 * persisted {@code staminaRegenDelayTicks} is set from
 * {@link dev.muon.combat_attributes.config.ConfigGeneral#staminaEmptyRegenDelay}
 * so the regen ticker pauses recovery during the configured exhaustion window.
 * Mana writes never touch this field.
 */
public final class PlayerResources {

    private PlayerResources() {}

    public static PlayerResourceData get(Player player) {
        return Services.PLATFORM.getPlayerResourceStore().get(player);
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

    /** Mana counterpart to {@link #canSpendStamina(Player)}. No lockout — purely a {@code > 0} check. */
    public static boolean canSpendMana(Player player) {
        return getMana(player) > 0.0F;
    }

    /**
     * Atomically gate-and-spend the configured cost. Returns {@code true} if the player
     * had any stamina (the spend went through, possibly taking them to zero and arming
     * the lockout) or {@code false} if stamina was already exhausted (no spend happened
     * and the caller should block its action). Costs of {@code <= 0} are treated as a
     * trivial success — useful for "feature disabled" config values without a separate
     * branch at every call site.
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
        int delay = nextStaminaRegenDelay(current.staminaRegenDelayTicks(), intentToDeplete);
        PlayerResourceData finalData = new PlayerResourceData(resolved, current.mana(), delay);
        if (finalData.equals(current)) return;
        Services.PLATFORM.getPlayerResourceStore().set(player, finalData);
    }

    /**
     * Arms the stamina regen delay timer to {@code max(current, ticks)} — never shortens
     * an existing longer delay. Used for non-depletion regen pauses (e.g. the post-attack
     * pause); the post-exhaustion lockout has its own arming inside {@link #setStamina}.
     * No-op when {@code ticks <= 0} or the existing delay already covers it.
     *
     * <p>Unlike the other public writes on this class, this method does not dispatch
     * {@code ChangeStaminaEvent} / {@code ChangeManaEvent}: neither pool's value is
     * changing, only the regen timer field. Listeners that gate on resource value
     * mutations would have nothing to react to.
     */
    public static void armStaminaRegenDelay(Player player, int ticks) {
        if (ticks <= 0) return;
        PlayerResourceData current = get(player);
        if (ticks <= current.staminaRegenDelayTicks()) return;
        Services.PLATFORM.getPlayerResourceStore().set(player,
                new PlayerResourceData(current.stamina(), current.mana(), ticks));
    }

    public static void setMana(Player player, float mana) {
        PlayerResourceData current = get(player);
        if (mana == current.mana()) return;
        float resolved = clamp(Services.PLATFORM.fireChangeMana(player, current.mana(), mana), getMaxMana(player));
        if (resolved == current.mana()) return;
        Services.PLATFORM.getPlayerResourceStore().set(player, current.withMana(resolved));
    }

    /**
     * Package-private write path for callers (i.e. the regen ticker) that already hold the
     * prior {@link PlayerResourceData} and have produced clamped values themselves. Skips
     * the re-fetch a public setter would perform, but still dispatches the per-resource
     * change events and re-clamps any listener-mutated value.
     *
     * <p>Trusts the caller's {@code next.staminaRegenDelayTicks()} as authoritative —
     * lockout arming is the responsibility of the public depletion paths
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
     * Lockout policy: arm the delay from config whenever the caller's intent was to
     * deplete (raw stamina argument {@code <= 0} and current was positive). Tracking
     * intent rather than the post-listener resolved value matters because the
     * {@code stamina_cost} multiplier can keep stamina from landing at exactly zero
     * even when the caller asked for a full depletion — without this, players in
     * that asymptotic regime would never trigger the recovery window.
     */
    private static int nextStaminaRegenDelay(int proposedDelay, boolean armLockout) {
        if (armLockout) return Math.max(proposedDelay, lockoutTicks());
        return proposedDelay;
    }

    private static int lockoutTicks() {
        double seconds = Configs.GENERAL.staminaEmptyRegenDelay.get();
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
