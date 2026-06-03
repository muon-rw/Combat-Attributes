package dev.muon.combat_attributes.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

/**
 * Fabric registration for the resource-anchor channel, both sides. Declares the
 * clientbound payload so the server can send it and the client can decode it.
 *
 * <p>The client receiver and per-tick extrapolation pump live in
 * {@link ResourceNetworkFabricClient}, not here: those handler lambdas reference
 * client-only types (Minecraft, LocalPlayer), and Fabric does not strip the synthetic
 * lambda methods of an {@code @Environment(CLIENT)} method. A dedicated server that
 * loaded this class (it calls {@link #register()} from common init) would then try to
 * resolve those client types and crash. Keeping them in a class the server never
 * references avoids that.
 */
public final class ResourceNetworkFabric {

    private ResourceNetworkFabric() {}

    /** Both environments: declare the clientbound payload so it can be sent and decoded. */
    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(ResourceAnchorPayload.TYPE, ResourceAnchorPayload.CODEC);
    }
}
