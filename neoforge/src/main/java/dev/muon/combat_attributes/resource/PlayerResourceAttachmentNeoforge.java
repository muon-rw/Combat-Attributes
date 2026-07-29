package dev.muon.combat_attributes.resource;

import dev.muon.combat_attributes.CombatAttributes;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * NeoForge attachment for {@link PlayerResourceData}.
 *
 * <p>Persistent (saved to disk via the codec) and synced to the owning client only:
 * the precise, per-tick stream behind that player's own HUD. The
 * {@code (holder, to) -> holder == to} predicate restricts {@code .sync()} to the owner.
 * Other players' pools reach a client through the throttled resource-anchor packet plus
 * client-side regen extrapolation ({@code ResourceSync} / {@code ClientResourceExtrapolator}),
 * not a per-tick broadcast to every tracker.
 */
public final class PlayerResourceAttachmentNeoforge {

    public static final DeferredRegister<AttachmentType<?>> REGISTRY =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, CombatAttributes.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerResourceData>> RESOURCES =
            REGISTRY.register("player_resources",
                    () -> AttachmentType.builder(() -> PlayerResourceData.DEFAULT)
                            .serialize(PlayerResourceData.CODEC.fieldOf("resources"))
                            .sync((holder, to) -> holder == to, PlayerResourceData.STREAM_CODEC)
                            .build()
            );

    private PlayerResourceAttachmentNeoforge() {}
}
