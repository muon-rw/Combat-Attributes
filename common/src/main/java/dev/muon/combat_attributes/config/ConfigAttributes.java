package dev.muon.combat_attributes.config;

import dev.muon.combat_attributes.CombatAttributes;
import dev.muon.combat_attributes.attribute.AttributeSpec;
import dev.muon.combat_attributes.attribute.AttributeSpec.StackingMode;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.config.Config;
import net.minecraft.resources.Identifier;

/**
 * Server-authoritative configuration for every Combat Attributes attribute.
 *
 * <p>One section per attribute, each holding {@code default} / {@code min} / {@code max},
 * a {@code stackingMode} ({@link StackingMode#LINEAR}, {@link StackingMode#SOFT_CAP},
 * {@link StackingMode#PROBABILISTIC}), and the soft-cap parameters {@code softCap} (M)
 * and {@code halfSaturation} (k). Loaded as
 * {@link me.fzzyhmstrs.fzzy_config.api.RegisterType#BOTH} so client and server stay in sync.
 *
 * <p>Calibration intent for this default pass:
 * <ul>
 *   <li><b>Crit chance</b> caps at 40% per source (M=0.4) and reaches ~36% at modifier
 *       sum x=0.5 — i.e. 50 stat points at 0.01/pt from Chronicles. Two independent
 *       40% sources combine via probabilistic union to ~64% (1 − 0.6²).</li>
 *   <li><b>Evasion</b> caps lower at 30% per source (M=0.3) so two maxed sources still
 *       leave the player exposed (~51%). Same shape, same k.</li>
 *   <li><b>Crit damage</b> keeps the existing M=5 / k=2 shape — additive on top of the
 *       1.5 base, so 6.5× max from a single source, ~16.5× across all three operation
 *       slots (rare to see in practice).</li>
 *   <li><b>Accuracy</b> is probabilistic but with M=1.0 and a slow k=2.0 — full
 *       accuracy is reachable but expensive.</li>
 *   <li><b>ranged_damage</b>, <b>lifesteal</b>, <b>draw_speed</b>, <b>arrow_velocity</b>
 *       are LINEAR (no soft cap; vanilla stacking).</li>
 * </ul>
 *
 * <p>File: <code>config/combat_attributes/combat_attributes-attributes.toml</code>
 */
public class ConfigAttributes extends Config {

    public ConfigAttributes() {
        super(Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, "attributes"));
    }

    // --- Melee ---

    @Comment("Probability (0–1) of a melee critical strike on attack. PROBABILISTIC stacking — caps at 40% per source; multiple sources combine via probabilistic union.")
    public AttributeSpec meleeCritChance = new AttributeSpec(0.0, 0.0, 1.0, StackingMode.PROBABILISTIC, 0.4, 0.05);

    @Comment("Damage multiplier applied to melee critical strikes. Default 1.5 (matches vanilla jump-crit). SOFT_CAP — each operation adds up to +5 above base, asymptote ~6.5× from a single source.")
    public AttributeSpec meleeCritDamage = new AttributeSpec(1.5, 1.0, 100.0, StackingMode.SOFT_CAP, 5.0, 2.0);

    // --- Ranged ---

    @Comment("Flat damage bonus added to non-magic projectile damage sources (excludes #c:is_magic). LINEAR — vanilla stacking.")
    public AttributeSpec rangedDamage = new AttributeSpec(0.0, -100.0, 1000.0, StackingMode.LINEAR, 0.0, 1.0);

    @Comment("Probability (0–1) of a ranged (projectile) critical strike. Separate roll from melee/magic. PROBABILISTIC — 40% per-source cap.")
    public AttributeSpec rangedCritChance = new AttributeSpec(0.0, 0.0, 1.0, StackingMode.PROBABILISTIC, 0.4, 0.05);

    @Comment("Damage multiplier applied to ranged critical strikes. Default 1.5. SOFT_CAP — same shape as melee crit damage.")
    public AttributeSpec rangedCritDamage = new AttributeSpec(1.5, 1.0, 100.0, StackingMode.SOFT_CAP, 5.0, 2.0);

    // --- Magic ---

    @Comment("Probability (0–1) of a magic critical strike on damage tagged #c:is_magic. Separate roll. PROBABILISTIC — 40% per-source cap.")
    public AttributeSpec magicCritChance = new AttributeSpec(0.0, 0.0, 1.0, StackingMode.PROBABILISTIC, 0.4, 0.05);

    @Comment("Damage multiplier applied to magic critical strikes. Default 1.5. SOFT_CAP — same shape as melee crit damage.")
    public AttributeSpec magicCritDamage = new AttributeSpec(1.5, 1.0, 100.0, StackingMode.SOFT_CAP, 5.0, 2.0);

    // --- Defensive ---

    @Comment("Probability (0–1) of dodging incoming damage of any source. PROBABILISTIC — capped at 30% per source, lower than crit chance so two maxed sources still leave the player ~50% exposed.")
    public AttributeSpec evasion = new AttributeSpec(0.0, 0.0, 1.0, StackingMode.PROBABILISTIC, 0.3, 0.05);

    @Comment("Fraction of damage dealt that heals the attacker. Applies only when the attacker is within their entity_interaction_range of the victim. LINEAR — vanilla stacking.")
    public AttributeSpec lifesteal = new AttributeSpec(0.0, 0.0, 1.0, StackingMode.LINEAR, 0.0, 1.0);

    // --- Bow physics ---

    @Comment("Bonus to bow draw speed and crossbow charge speed. +0.5 = 50% faster (held time effectively scaled by 1.5; crossbow charge duration divided by 1.5). LINEAR — vanilla stacking.")
    public AttributeSpec drawSpeed = new AttributeSpec(0.0, 0.0, 5.0, StackingMode.LINEAR, 0.0, 1.0);

    @Comment("Bonus to fired arrow velocity for both bows and crossbows. +0.5 = 50% faster arrow. LINEAR — vanilla stacking. Note: AbstractArrow base damage scales with velocity, so this also indirectly increases hit damage on direct projectiles. Effectively unclamped (max=1_000_000).")
    public AttributeSpec arrowVelocity = new AttributeSpec(0.0, 0.0, 1_000_000.0, StackingMode.LINEAR, 0.0, 1.0);

    @Comment("Reduces fired arrow inaccuracy for both bows and crossbows. +1.0 = perfect accuracy (no spread). PROBABILISTIC stacking with M=1.0 and a slow k=2.0 — full accuracy reachable but expensive.")
    public AttributeSpec accuracy = new AttributeSpec(0.0, 0.0, 1.0, StackingMode.PROBABILISTIC, 1.0, 2.0);
}
