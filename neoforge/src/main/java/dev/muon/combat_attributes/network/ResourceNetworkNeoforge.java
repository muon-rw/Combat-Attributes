package dev.muon.combat_attributes.network;

import dev.muon.combat_attributes.CombatAttributes;
import dev.muon.combat_attributes.resource.ClientResourceExtrapolator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * NeoForge registration for the resource-anchor channel: declares the
 * clientbound payload and routes received anchors into the client extrapolator.
 */
@EventBusSubscriber(modid = CombatAttributes.MOD_ID)
public final class ResourceNetworkNeoforge {

    private ResourceNetworkNeoforge() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(CombatAttributes.MOD_ID).versioned("1");
        registrar.playToClient(ResourceAnchorPayload.TYPE, ResourceAnchorPayload.CODEC, ResourceNetworkNeoforge::handle);
    }

    private static void handle(ResourceAnchorPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientResourceExtrapolator.receiveAnchor(context.player().level(), payload.entityId(), payload.data());
            }
        });
    }
}
