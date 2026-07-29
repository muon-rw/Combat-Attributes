package dev.muon.combat_attributes.feature;

import dev.muon.combat_attributes.CombatAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = CombatAttributes.MOD_ID)
public final class HealthRegenEventsNeoforge {

    private HealthRegenEventsNeoforge() {}

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        // Post fires on both logical sides; filter the client so regen stays server-authoritative.
        if (event.getEntity() instanceof LivingEntity living && !living.level().isClientSide()) {
            HealthRegenTicker.onLivingTick(living);
        }
    }
}
