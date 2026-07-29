package dev.muon.combat_attributes.network;

import dev.muon.combat_attributes.resource.ClientResourceExtrapolator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Client-only Fabric wiring for the resource-anchor channel: applies incoming anchors and
 * advances tracked players' pools each tick. Referenced only from {@code CombatAttributesFabricClient}
 * (a {@code ClientModInitializer}), so a dedicated server never loads this class or resolves the
 * client-only types its handler lambdas touch. The payload type itself is declared in
 * {@link ResourceNetworkFabric#register()}, which runs on both sides.
 */
@Environment(EnvType.CLIENT)
public final class ResourceNetworkFabricClient {

    private ResourceNetworkFabricClient() {}

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(ResourceAnchorPayload.TYPE, (payload, context) ->
                context.client().execute(() ->
                        ClientResourceExtrapolator.receiveAnchor(context.client().level, payload.entityId(), payload.data())));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level != null && client.player != null) {
                ClientResourceExtrapolator.tick(client.level, client.player);
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientResourceExtrapolator.clear());
    }
}
