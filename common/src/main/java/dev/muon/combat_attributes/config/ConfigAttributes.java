package dev.muon.combat_attributes.config;

import dev.muon.combat_attributes.CombatAttributes;
import dev.muon.combat_attributes.attribute.AttributeSpec;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.config.Config;
import net.minecraft.resources.Identifier;

/**
 * Server-authoritative configuration for every Combat Attributes attribute.
 *
 * <p>One section per attribute, each holding {@code default} / {@code min} / {@code max},
 * a {@code diminishing} toggle, and three per-operation formulas. Loaded as
 * {@link me.fzzyhmstrs.fzzy_config.api.RegisterType#BOTH} so client and server stay in sync.
 *
 * <p>For chance attributes ({@code crit_chance}, {@code evasion}): the default formula
 * asymptotes at 1.0; a larger denominator constant in {@code x/(x+k)} = stronger
 * diminishing (harder to approach 100%). For damage-multiplier attributes
 * ({@code crit_damage}): {@code 5*x/(x+2)} caps the bonus at +5 above the 1.5 base
 * (so 6.5× max crit damage), with linear-ish behavior at small {@code x}.
 *
 * <p>File: <code>config/combat_attributes/combat_attributes-attributes.toml</code>
 */
public class ConfigAttributes extends Config {

    public ConfigAttributes() {
        super(Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, "attributes"));
    }

    // --- Melee ---

    @Comment("Probability (0–1) of a melee critical strike on attack. Diminishes — extremely hard to reach 100%.")
    public AttributeSpec meleeCritChance = new AttributeSpec(0.0, 0.0, 1.0, "x/(x+5)", true);

    @Comment("Damage multiplier applied to melee critical strikes. Default 1.5 (matches vanilla jump-crit). Diminishes with a high softcap.")
    public AttributeSpec meleeCritDamage = new AttributeSpec(1.5, 1.0, 100.0, "5*x/(x+2)", true);

    // --- Ranged ---

    @Comment("Flat damage bonus added to non-magic projectile damage sources (excludes #c:is_magic). Vanilla stacking by default.")
    public AttributeSpec rangedDamage = new AttributeSpec(0.0, -100.0, 1000.0, "x", false);

    @Comment("Probability (0–1) of a ranged (projectile) critical strike. Separate roll from melee/magic. Diminishes.")
    public AttributeSpec rangedCritChance = new AttributeSpec(0.0, 0.0, 1.0, "x/(x+5)", true);

    @Comment("Damage multiplier applied to ranged critical strikes. Default 1.5. Diminishes with a high softcap.")
    public AttributeSpec rangedCritDamage = new AttributeSpec(1.5, 1.0, 100.0, "5*x/(x+2)", true);

    // --- Magic ---

    @Comment("Probability (0–1) of a magic critical strike on damage tagged #c:is_magic. Separate roll. Diminishes.")
    public AttributeSpec magicCritChance = new AttributeSpec(0.0, 0.0, 1.0, "x/(x+5)", true);

    @Comment("Damage multiplier applied to magic critical strikes. Default 1.5. Diminishes with a high softcap.")
    public AttributeSpec magicCritDamage = new AttributeSpec(1.5, 1.0, 100.0, "5*x/(x+2)", true);

    // --- Defensive ---

    @Comment("Probability (0–1) of dodging incoming damage of any source. Diminishes strongly — very hard to approach 100%.")
    public AttributeSpec evasion = new AttributeSpec(0.0, 0.0, 1.0, "x/(x+10)", true);

    @Comment("Fraction of damage dealt that heals the attacker. Applies only when the attacker is within their entity_interaction_range of the victim. Vanilla stacking.")
    public AttributeSpec lifesteal = new AttributeSpec(0.0, 0.0, 1.0, "x", false);

    // --- Bow physics ---

    @Comment("Bonus to bow draw speed and crossbow charge speed. +0.5 = 50% faster (held time effectively scaled by 1.5; crossbow charge duration divided by 1.5). Vanilla stacking.")
    public AttributeSpec drawSpeed = new AttributeSpec(0.0, 0.0, 5.0, "x", false);

    @Comment("Bonus to fired arrow velocity for both bows and crossbows. +0.5 = 50% faster arrow. Vanilla stacking. Note: AbstractArrow base damage scales with velocity, so this also indirectly increases hit damage on direct projectiles.")
    public AttributeSpec arrowVelocity = new AttributeSpec(0.0, 0.0, 5.0, "x", false);

    @Comment("Reduces fired arrow inaccuracy for both bows and crossbows. +0.5 = 50% less spread; +1.0 = perfect accuracy (no spread). Mild diminishing.")
    public AttributeSpec accuracy = new AttributeSpec(0.0, 0.0, 1.0, "x/(x+2)", true);
}
