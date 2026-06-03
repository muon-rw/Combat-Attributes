package dev.muon.combat_attributes.client;

import dev.muon.combat_attributes.CombatAttributes;
import dev.muon.combat_attributes.resource.ClientResourceExtrapolator;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Client-side pump for the resource extrapolator on NeoForge: advances tracked
 * players' pools each client tick (Fabric's {@code END_CLIENT_TICK} counterpart)
 * and drops stashed anchors on disconnect.
 */
@EventBusSubscriber(modid = CombatAttributes.MOD_ID, value = Dist.CLIENT)
public final class ClientResourceEventsNeoforge {

    private ClientResourceEventsNeoforge() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            ClientResourceExtrapolator.tick(mc.level, mc.player);
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientResourceExtrapolator.clear();
    }
}
