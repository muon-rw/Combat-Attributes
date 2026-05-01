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
 * <p>Skips entirely when both pools are already at max — saves a redundant
 * read/clamp/write per tick on rested players.
 */
public final class PlayerResourceTicker {

    private static final float SECONDS_PER_TICK = 1.0F / 20.0F;

    private PlayerResourceTicker() {}

    public static void onPlayerTick(ServerPlayer player) {
        float maxStamina = PlayerResources.getMaxStamina(player);
        float maxMana    = PlayerResources.getMaxMana(player);
        PlayerResourceData data = PlayerResources.get(player);

        boolean staminaFull = data.stamina() >= maxStamina;
        boolean manaFull    = data.mana()    >= maxMana;
        if (staminaFull && manaFull) return;

        float staminaRegen = (float) ModAttributes.valueOrDefault(player, ModAttributes.staminaRegen());
        float manaRegen    = (float) ModAttributes.valueOrDefault(player, ModAttributes.manaRegen());

        float nextStamina = staminaFull ? data.stamina() : Math.min(data.stamina() + staminaRegen * SECONDS_PER_TICK, maxStamina);
        float nextMana    = manaFull    ? data.mana()    : Math.min(data.mana()    + manaRegen    * SECONDS_PER_TICK, maxMana);

        PlayerResources.writeIfChanged(player, data, new PlayerResourceData(nextStamina, nextMana));
    }
}
