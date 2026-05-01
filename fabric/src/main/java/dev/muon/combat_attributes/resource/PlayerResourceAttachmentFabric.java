package dev.muon.combat_attributes.resource;

import dev.muon.combat_attributes.CombatAttributes;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

/**
 * Fabric attachment for {@link PlayerResourceData}.
 *
 * <p>Persistent (saved with the player NBT via the codec) and auto-synced to
 * the owning client on change. {@link AttachmentSyncPredicate#targetOnly()}
 * keeps the payload off other tracking clients — they have no need for a
 * given player's resource pool.
 */
public final class PlayerResourceAttachmentFabric {

    public static final AttachmentType<PlayerResourceData> RESOURCES = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, "player_resources"),
            builder -> builder
                    .initializer(() -> PlayerResourceData.DEFAULT)
                    .persistent(PlayerResourceData.CODEC)
                    .syncWith(PlayerResourceData.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
    );

    private PlayerResourceAttachmentFabric() {}

    /** Forces class-load + registration during early mod init. */
    public static void init() {
        CombatAttributes.LOG.debug("Registered Fabric player resource attachment: {}", RESOURCES.identifier());
    }
}
