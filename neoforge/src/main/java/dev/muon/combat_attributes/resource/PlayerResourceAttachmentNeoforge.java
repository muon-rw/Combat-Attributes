package dev.muon.combat_attributes.resource;

import dev.muon.combat_attributes.CombatAttributes;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * NeoForge attachment for {@link PlayerResourceData}.
 *
 * <p>Persistent (saved to disk via the codec) and synced (sent automatically
 * to the owning client when changed). Same shape as Chronicles Leveling's
 * {@code PlayerLevelAttachmentNeoforge.LEVEL} — Codec for disk, StreamCodec
 * for sync.
 */
public final class PlayerResourceAttachmentNeoforge {

    public static final DeferredRegister<AttachmentType<?>> REGISTRY =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, CombatAttributes.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerResourceData>> RESOURCES =
            REGISTRY.register("player_resources",
                    () -> AttachmentType.builder(() -> PlayerResourceData.DEFAULT)
                            .serialize(PlayerResourceData.CODEC.fieldOf("resources"))
                            .sync(PlayerResourceData.STREAM_CODEC)
                            .build()
            );

    private PlayerResourceAttachmentNeoforge() {}
}
