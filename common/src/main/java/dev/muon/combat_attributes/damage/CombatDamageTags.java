package dev.muon.combat_attributes.damage;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

/**
 * Damage type tags used by Combat Attributes' damage pipeline.
 *
 * <p>{@link #IS_MAGIC} is the conventional cross-mod {@code #c:is_magic} tag — populated by
 * spell mods (Iron's Spells, Apoli/Origins effect damage, etc.) to flag damage that should
 * be treated as magical. Vanilla doesn't ship this tag, but it's standard in the Fabric/Forge
 * ecosystem's {@code c} (Common) namespace.
 */
public final class CombatDamageTags {

    private CombatDamageTags() {}

    public static final TagKey<DamageType> IS_MAGIC = TagKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath("c", "is_magic")
    );
}
