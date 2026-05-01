package dev.muon.combat_attributes.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * The per-player runtime state for the resource (stamina/mana) systems.
 *
 * <p>Kept as a single record rather than two separate attachments so a single
 * sync packet delivers the whole picture and there's no inter-attachment
 * ordering risk on client load — same convention as Chronicles Leveling's
 * {@code PlayerLevelData}.
 *
 * <p>Maxes and regen rates are read from {@link dev.muon.combat_attributes.attribute.ModAttributes}
 * at use time; they are not persisted here. Only the live consumable values
 * are stored, so respeccing the player's max attributes recalculates the
 * displayed bar size without touching this record.
 */
public record PlayerResourceData(float stamina, float mana) {

    public static final PlayerResourceData DEFAULT = new PlayerResourceData(0.0F, 0.0F);

    public static final Codec<PlayerResourceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("stamina", 0.0F).forGetter(PlayerResourceData::stamina),
            Codec.FLOAT.optionalFieldOf("mana",    0.0F).forGetter(PlayerResourceData::mana)
    ).apply(instance, PlayerResourceData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerResourceData> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT, PlayerResourceData::stamina,
                    ByteBufCodecs.FLOAT, PlayerResourceData::mana,
                    PlayerResourceData::new
            );

    public PlayerResourceData withStamina(float stamina) {
        return new PlayerResourceData(stamina, this.mana);
    }

    public PlayerResourceData withMana(float mana) {
        return new PlayerResourceData(this.stamina, mana);
    }
}
