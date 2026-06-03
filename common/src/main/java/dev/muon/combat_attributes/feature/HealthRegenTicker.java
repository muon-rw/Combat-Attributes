package dev.muon.combat_attributes.feature;

import dev.muon.combat_attributes.attribute.ModAttributes;
import net.minecraft.world.entity.LivingEntity;

/**
 * Per-tick health regeneration driven by the {@code health_regeneration} attribute.
 * Attaches to every living entity, so mobs with the attribute regenerate too;
 * loader code feeds living entities here from its own server-side tick event
 * (NeoForge {@code EntityTickEvent.Post}, Fabric {@code END_SERVER_TICK}).
 *
 * <p>Unlike stamina/mana regen, this is <em>always</em> applied while the entity
 * is below max health; no delay timer or pause condition gates it.
 *
 * <p>Attribute units are health <em>per second</em>, divided across 20 ticks so
 * fractional rates (e.g. 0.5/sec) accumulate predictably in the float health value,
 * mirroring {@link dev.muon.combat_attributes.resource.PlayerResourceTicker}'s regen math.
 */
public final class HealthRegenTicker {

    private static final float SECONDS_PER_TICK = 1.0F / 20.0F;

    private HealthRegenTicker() {}

    /** Server-side only; callers guarantee the logical side before invoking. */
    public static void onLivingTick(LivingEntity entity) {
        if (!entity.isAlive()) return;
        if (entity.getHealth() >= entity.getMaxHealth()) return;

        float regenPerSecond = (float) ModAttributes.valueOrDefault(entity, ModAttributes.healthRegeneration());
        if (regenPerSecond <= 0.0F) return;

        entity.heal(regenPerSecond * SECONDS_PER_TICK);
    }
}
