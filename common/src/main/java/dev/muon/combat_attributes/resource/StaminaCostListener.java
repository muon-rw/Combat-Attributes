package dev.muon.combat_attributes.resource;

import dev.muon.combat_attributes.attribute.ModAttributes;
import net.minecraft.world.entity.player.Player;

/**
 * Common helper for the {@code stamina_cost} attribute multiplier. Each
 * loader registers a listener on its native {@code ChangeStamina} event /
 * callback that delegates here, so all stamina drains (first-party consumers,
 * third-party consumers, and the regen-tick continuous drains alike) pick up
 * the multiplier through the same single code path.
 *
 * <p>Only negative deltas (drains) are scaled. Positive deltas (regen,
 * heals, max-attribute bumps) and no-ops pass through unchanged. The
 * adjusted value remains subject to the post-listener {@code [0, max]}
 * re-clamp performed by {@link PlayerResources}, so a multiplier above 1
 * cannot push the result below zero in problematic ways.
 */
public final class StaminaCostListener {

    private StaminaCostListener() {}

    public static float applyCostMultiplier(Player player, float oldValue, float newValue) {
        if (newValue >= oldValue) return newValue;
        double mult = ModAttributes.valueOrDefault(player, ModAttributes.staminaCost());
        if (mult == 1.0) return newValue;
        float drain = oldValue - newValue;
        return oldValue - (float) (drain * mult);
    }
}
