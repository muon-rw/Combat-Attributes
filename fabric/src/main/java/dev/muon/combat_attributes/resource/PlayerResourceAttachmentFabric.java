package dev.muon.combat_attributes.resource;

import dev.muon.combat_attributes.CombatAttributes;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

/**
 * Fabric attachment for {@link PlayerResourceData}.
 *
 * <p>Persistent (saved with the player NBT via the codec) and synced to the owning
 * client only ({@link AttachmentSyncPredicate#targetOnly()}): the precise, per-tick
 * stream that drives that player's own HUD. <em>Other</em> players' pools reach a
 * client through the throttled, event-driven resource-anchor packet plus client-side
 * regen extrapolation (see {@code ResourceSync} / {@code ClientResourceExtrapolator}),
 * not this attachment, so a crowd of regenerating players doesn't multiply per-tick
 * traffic across every tracker.
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
