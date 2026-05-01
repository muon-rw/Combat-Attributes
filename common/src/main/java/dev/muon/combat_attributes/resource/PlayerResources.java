package dev.muon.combat_attributes.resource;

import dev.muon.combat_attributes.attribute.ModAttributes;
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
 * the last write. Write methods clamp on the way in for the same reason.
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

    public static void setStamina(Player player, float stamina) {
        PlayerResourceData current = get(player);
        float clamped = clamp(stamina, getMaxStamina(player));
        if (clamped == current.stamina()) return;
        Services.PLATFORM.getPlayerResourceStore().set(player, current.withStamina(clamped));
    }

    public static void setMana(Player player, float mana) {
        PlayerResourceData current = get(player);
        float clamped = clamp(mana, getMaxMana(player));
        if (clamped == current.mana()) return;
        Services.PLATFORM.getPlayerResourceStore().set(player, current.withMana(clamped));
    }

    /**
     * Writes both fields in a single attachment update — preferred when both are
     * changing in the same tick (e.g. the regen ticker) so the auto-sync produces
     * one network packet instead of two.
     */
    public static void set(Player player, float stamina, float mana) {
        PlayerResourceData current = get(player);
        float s = clamp(stamina, getMaxStamina(player));
        float m = clamp(mana,    getMaxMana(player));
        if (s == current.stamina() && m == current.mana()) return;
        Services.PLATFORM.getPlayerResourceStore().set(player, new PlayerResourceData(s, m));
    }

    /**
     * Direct write path for callers that already hold the prior {@link PlayerResourceData}
     * and have produced clamped values themselves (e.g. the regen ticker, which had to
     * read the max attributes anyway to gate its early-return). Skips the re-fetch and
     * re-clamp that {@link #set(Player, float, float)} performs.
     */
    static void writeIfChanged(Player player, PlayerResourceData previous, PlayerResourceData next) {
        if (next.stamina() == previous.stamina() && next.mana() == previous.mana()) return;
        Services.PLATFORM.getPlayerResourceStore().set(player, next);
    }

    private static float clamp(float v, float max) {
        if (max <= 0.0F) return 0.0F;
        if (v < 0.0F) return 0.0F;
        if (v > max) return max;
        return v;
    }
}
