package dev.muon.combat_attributes.damage;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public final class CombatDamageTags {

    private CombatDamageTags() {}

    // Conventional cross-mod #c:is_magic tag, populated by spell mods (Iron's Spells,
    // Apoli/Origins effect damage, etc.) to flag damage as magical.
    // Vanilla and platforms do NOT ship this tag but it's the go-to.
    public static final TagKey<DamageType> IS_MAGIC = TagKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath("c", "is_magic")
    );
}
