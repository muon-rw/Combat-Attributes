package dev.muon.combat_attributes.feature;

import dev.muon.combat_attributes.config.Configs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;

public final class LegacyHunger {

    private LegacyHunger() {}

    public static boolean isEnabled() {
        return Configs.GENERAL != null && Configs.GENERAL.legacyHunger.get();
    }

    /**
     * Half-hearts of healing for a food with the given nutrition + saturation. Returned regardless
     * of whether the feature is currently enabled; callers that want the active value should gate
     * on {@link #isEnabled()} themselves. AppleSkin compat can call this unconditionally to render
     * the heart preview the same way the runtime applies it.
     */
    public static float computeHeal(int nutrition, float saturation) {
        if (Configs.GENERAL == null) return 0.0F;
        double perN = Configs.GENERAL.legacyHungerHealPerNutrition.get();
        double perS = Configs.GENERAL.legacyHungerHealPerSaturation.get();
        // Round to whole half-hearts so the AppleSkin tooltip, HUD preview, and applied heal agree on one integer.
        return (float) Math.round(perN * nutrition + perS * saturation);
    }

    public static float computeHeal(FoodProperties food) {
        return computeHeal(food.nutrition(), food.saturation());
    }

    /**
     * Applies the legacy-hunger healing to {@code user} for the given food. Gated on
     * {@link #isEnabled()} and {@code user instanceof Player}; safe to call unconditionally from a
     * mixin head injection.
     *
     * @return true if healing was applied (mixin should cancel the vanilla food/saturation update)
     */
    public static boolean applyOnConsume(LivingEntity user, FoodProperties food) {
        if (!isEnabled()) return false;
        if (!(user instanceof Player player)) return false;
        float heal = computeHeal(food);
        if (heal > 0.0F) player.heal(heal);
        return true;
    }
}
