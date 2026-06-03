package dev.muon.combat_attributes.feature;

import dev.muon.combat_attributes.CombatAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * NeoForge per-entity tick hook for {@code health_regeneration}. {@code EntityTickEvent.Post}
 * fires for every entity each tick after its own tick has run, so we get the canonical
 * end-of-tick health before regenerating. Fires on both logical sides, so non-server
 * ticks are filtered out before touching health (authoritative on the server only).
 */
@EventBusSubscriber(modid = CombatAttributes.MOD_ID)
public final class HealthRegenEventsNeoforge {

    private HealthRegenEventsNeoforge() {}

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living && !living.level().isClientSide()) {
            HealthRegenTicker.onLivingTick(living);
        }
    }
}
