package dev.muon.combat_attributes.network;

import dev.muon.combat_attributes.CombatAttributes;
import dev.muon.combat_attributes.resource.PlayerResourceData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server &rarr; client "anchor" carrying one player's resource pool. Sent only on
 * perturbations the client can't predict (a spend/drain), when a client starts
 * tracking the player, and as a periodic keepalive; <em>not</em> every tick.
 * Between anchors the client reconstructs regen locally (see
 * {@link dev.muon.combat_attributes.resource.ClientResourceExtrapolator}), like
 * vanilla extrapolating a mob effect's duration instead of streaming it.
 *
 * <p>The owning client gets its own pool through the attachment sync, so these
 * anchors only go to the player's trackers.
 */
public record ResourceAnchorPayload(int entityId, PlayerResourceData data) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ResourceAnchorPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, "resource_anchor"));

    public static final StreamCodec<FriendlyByteBuf, ResourceAnchorPayload> CODEC =
            CustomPacketPayload.codec(ResourceAnchorPayload::write, ResourceAnchorPayload::new);

    /** Decode constructor (argument order matches {@link #write}: id, stamina, mana, delay). */
    public ResourceAnchorPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt(), new PlayerResourceData(buf.readFloat(), buf.readFloat(), buf.readVarInt()));
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeFloat(data.stamina());
        buf.writeFloat(data.mana());
        buf.writeVarInt(data.staminaRegenDelayTicks());
    }

    @Override
    public CustomPacketPayload.Type<ResourceAnchorPayload> type() {
        return TYPE;
    }
}
