package dev.muon.combat_attributes.resource;

import dev.muon.combat_attributes.attribute.ModAttributes;
import net.minecraft.server.level.ServerPlayer;

/**
 * Per-tick stamina + mana regeneration. Runs server-authoritatively; the
 * resulting attachment update auto-syncs to the owning client.
 *
 * <p>Regen attribute units are <em>per second</em>, distributed across 20
 * ticks so fractional rates (e.g. 0.5/sec) accumulate predictably. Both
 * resources are written in a single {@code set(...)} call so a single sync
 * packet covers the tick.
 *
 * <p>Skips entirely when both pools sit exactly at max — saves a redundant
 * read/clamp/write per tick on rested players. Values that are <em>over</em>
 * max (e.g. the player's max attribute dropped after a buff expired) are
 * clamped down to max here so the persisted state stops drifting.
 */
public final class PlayerResourceTicker {

    private static final float SECONDS_PER_TICK = 1.0F / 20.0F;

    private PlayerResourceTicker() {}

    public static void onPlayerTick(ServerPlayer player) {
        float maxStamina = PlayerResources.getMaxStamina(player);
        float maxMana    = PlayerResources.getMaxMana(player);
        PlayerResourceData data = PlayerResources.get(player);

        if (data.stamina() == maxStamina && data.mana() == maxMana) return;

        float staminaRegen = (float) ModAttributes.valueOrDefault(player, ModAttributes.staminaRegen());
        float manaRegen    = (float) ModAttributes.valueOrDefault(player, ModAttributes.manaRegen());

        float nextStamina = data.stamina() > maxStamina ? maxStamina
                : Math.min(data.stamina() + staminaRegen * SECONDS_PER_TICK, maxStamina);
        float nextMana    = data.mana() > maxMana ? maxMana
                : Math.min(data.mana() + manaRegen * SECONDS_PER_TICK, maxMana);

        PlayerResources.writeIfChanged(player, data, new PlayerResourceData(nextStamina, nextMana));
    }
}
