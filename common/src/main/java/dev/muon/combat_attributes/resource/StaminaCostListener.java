package dev.muon.combat_attributes.resource;

import dev.muon.combat_attributes.attribute.ModAttributes;
import net.minecraft.world.entity.player.Player;

// Each loader's native ChangeStamina event delegates here so all stamina drains share one code path.
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
