package dev.muon.combat_attributes.config;

import dev.muon.combat_attributes.CombatAttributes;
import dev.muon.combat_attributes.attribute.AttributeSpec;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.config.Config;
import net.minecraft.resources.Identifier;

/**
 * Server-authoritative configuration for every Combat Attributes attribute.
 *
 * <p>One section per attribute, each holding {@code default} / {@code min} / {@code max}
 * and three diminishing formulas (one per modifier operation). Loaded as
 * {@link me.fzzyhmstrs.fzzy_config.api.RegisterType#BOTH} so client and server stay in sync.
 *
 * <p>Formula variable {@code x} is the sum of modifier amounts for that operation. Use
 * {@code "x"} for plain linear stacking; use a saturating formula like
 * {@code "1 - 1/(1 + x)"} for diminishing returns.
 *
 * <p>File: <code>config/combat_attributes/combat_attributes-attributes.toml</code>
 */
public class ConfigAttributes extends Config {

    public ConfigAttributes() {
        super(Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, "attributes"));
    }

    @Comment("Probability (0–1) that an attack is a critical hit.")
    public AttributeSpec critChance = new AttributeSpec(0.0, 0.0, 1.0, "1 - 1/(1 + x)");

    @Comment("Multiplier applied to damage on a critical hit. Default 1.0 = no bonus.")
    public AttributeSpec critDamage = new AttributeSpec(1.0, 0.0, 100.0, "x");

    @Comment("Fraction of damage dealt that heals the attacker.")
    public AttributeSpec lifesteal = new AttributeSpec(0.0, 0.0, 1.0, "1 - 1/(1 + x*2)");
}
