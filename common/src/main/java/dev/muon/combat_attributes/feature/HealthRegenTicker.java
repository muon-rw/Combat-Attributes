package dev.muon.combat_attributes.feature;

import dev.muon.combat_attributes.attribute.ModAttributes;
import net.minecraft.world.entity.LivingEntity;

/**
 * Regenerates any living entity carrying the {@code health_regen} attribute, not just players.
 * The attribute is health per second, divided across 20 ticks.
 */
public final class HealthRegenTicker {

    private static final float SECONDS_PER_TICK = 1.0F / 20.0F;

    private HealthRegenTicker() {}

    /** Server-side only; callers guarantee the logical side before invoking. */
    public static void onLivingTick(LivingEntity entity) {
        if (!entity.isAlive()) return;
        if (entity.getHealth() >= entity.getMaxHealth()) return;

        float regenPerSecond = (float) ModAttributes.valueOrDefault(entity, ModAttributes.healthRegen());
        if (regenPerSecond <= 0.0F) return;

        entity.heal(regenPerSecond * SECONDS_PER_TICK);
    }
}
