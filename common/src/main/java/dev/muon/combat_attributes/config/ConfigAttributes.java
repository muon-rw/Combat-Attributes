package dev.muon.combat_attributes.config;

import dev.muon.combat_attributes.CombatAttributes;
import dev.muon.combat_attributes.attribute.AttributeSpec;
import dev.muon.combat_attributes.attribute.AttributeSpec.StackingMode;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.annotations.TomlHeaderComment;
import me.fzzyhmstrs.fzzy_config.annotations.Version;
import me.fzzyhmstrs.fzzy_config.config.Config;
import net.minecraft.resources.Identifier;

/**
 * Server-authoritative configuration for every Combat Attributes attribute. Loaded as
 * {@link me.fzzyhmstrs.fzzy_config.api.RegisterType#BOTH} so client and server stay in sync.
 * Per-attribute fields: {@code default} / {@code min} / {@code max}, a {@code stackingMode}
 * ({@link StackingMode#LINEAR} / {@link StackingMode#SOFT_CAP} / {@link StackingMode#PROBABILISTIC}),
 * and soft-cap parameters {@code softCap} (M) and {@code halfSaturation} (k).
 *
 * <p>The end-user-facing explanation of M, k, and how operation slots combine lives in the
 * {@link TomlHeaderComment} annotations below; they're rendered into the generated TOML so
 * pack authors can read them inline while editing.
 *
 * <p>File: <code>config/combat_attributes/combat_attributes-attributes.toml</code>
 */
@TomlHeaderComment(text = "===== Combat Attributes =====")
@TomlHeaderComment(text = "Per-attribute: default value, bounds, stacking mode, and soft-cap shape (M, k).")
@TomlHeaderComment(text = "")
@TomlHeaderComment(text = "--- Stacking modes ---")
@TomlHeaderComment(text = "LINEAR        : vanilla math. M and k are ignored.")
@TomlHeaderComment(text = "SOFT_CAP      : diminishing returns, capped near M. For additive bonuses like crit damage.")
@TomlHeaderComment(text = "PROBABILISTIC : diminishing, sources combine so you never quite hit 100%. For chance stats.")
@TomlHeaderComment(text = "                Two 40% sources -> 64%, three -> 78%.")
@TomlHeaderComment(text = "MULTIPLICATIVE: for 'lower is better' multipliers like mana cost. Reductions diminish and")
@TomlHeaderComment(text = "                stack multiplicatively (two 30% off -> 51% off); increases pass through.")
@TomlHeaderComment(text = "")
@TomlHeaderComment(text = "Each operation slot is one source and has its own per-source cap; you can't dodge it by")
@TomlHeaderComment(text = "switching operation type.")
@TomlHeaderComment(text = "")
@TomlHeaderComment(text = "--- Tuning M (softCap) and k (halfSaturation), non-linear modes only ---")
@TomlHeaderComment(text = "  M = per-source ceiling. For chance stats it's the probability cap (0.4 = 40%). For")
@TomlHeaderComment(text = "      multipliers like crit damage it's added on top of base (M=5 over base=1.5 -> 6.5x).")
@TomlHeaderComment(text = "  k = how soft the ceiling is. Small k = nearly a hard cap. Large k = diminishing kicks")
@TomlHeaderComment(text = "      in early. Rough guide: k = M*0.1 keeps ~90% of M linear, k = M*0.3 keeps ~70%.")
@TomlHeaderComment(text = "")
@TomlHeaderComment(text = "--- Default calibration ---")
@TomlHeaderComment(text = "Crit chance : M=0.4, k=0.05 (~36% at 50 stat points; two sources -> 64%).")
@TomlHeaderComment(text = "Evasion     : M=0.3, k=0.05 (lower cap; two maxed sources still leave ~51% exposed).")
@TomlHeaderComment(text = "Crit damage : M=5,   k=2    (6.5x max from one source).")
@TomlHeaderComment(text = "Accuracy    : M=1.0, k=2.0  (full accuracy reachable but expensive).")
@TomlHeaderComment(text = "Ranged damage, lifesteal, draw speed, arrow velocity: LINEAR.")
@Version(version = 1)
public class ConfigAttributes extends Config {

    public ConfigAttributes() {
        super(Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, "attributes"));
    }

    // --- Melee ---

    @Comment("Chance (0-1) of a melee crit. Diminishing, ~50% per-source cap.")
    public AttributeSpec meleeCritChance = new AttributeSpec(0.0, 0.0, 1.0, StackingMode.PROBABILISTIC, 0.5, 0.15);

    @Comment("Melee crit damage multiplier. Default 1.5 (vanilla jump-crit). Diminishing, ~6.5x max per source.")
    public AttributeSpec meleeCritDamage = new AttributeSpec(1.5, 1.0, 100.0, StackingMode.SOFT_CAP, 1, 0.4);

    // --- Ranged ---

    @Comment("Flat damage added to non-magic projectiles (excludes #c:is_magic). Linear.")
    public AttributeSpec rangedDamage = new AttributeSpec(0.0, -100.0, 1000.0, StackingMode.LINEAR, 0.0, 1.0);

    @Comment("Chance (0-1) of a ranged crit (separate roll from melee/magic). Diminishing, ~50% per-source cap.")
    public AttributeSpec rangedCritChance = new AttributeSpec(0.0, 0.0, 1.0, StackingMode.PROBABILISTIC, 0.5, 0.15);

    @Comment("Ranged crit damage multiplier. Default 1.5. Diminishing, ~6.5x max per source.")
    public AttributeSpec rangedCritDamage = new AttributeSpec(1.5, 1.0, 100.0, StackingMode.SOFT_CAP, 1.0, 0.4);

    // --- Magic ---

    @Comment("Chance (0-1) of a magic crit on #c:is_magic damage (separate roll). Diminishing, ~50% per-source cap.")
    public AttributeSpec magicCritChance = new AttributeSpec(0.0, 0.0, 1.0, StackingMode.PROBABILISTIC, 0.5, 0.15);

    @Comment("Magic crit damage multiplier. Default 1.5. Diminishing, ~6.5x max per source.")
    public AttributeSpec magicCritDamage = new AttributeSpec(1.5, 1.0, 100.0, StackingMode.SOFT_CAP, 1.0, 0.4);

    @Comment("Magic damage multiplier (the magic analog of attack damage). Default 1.0. No built-in hooks; for spell mods to read. Linear.")
    public AttributeSpec magicPower = new AttributeSpec(1.0, 0.0, 1000.0, StackingMode.LINEAR, 0.0, 1.0);

    // --- Defensive ---

    @Comment("Chance (0-1) to dodge any incoming damage. Diminishing, ~30% per-source cap (two maxed sources still leave ~50% exposed).")
    public AttributeSpec evasion = new AttributeSpec(0.0, 0.0, 1.0, StackingMode.PROBABILISTIC, 0.3, 0.15);

    @Comment("Fraction (0-1) of damage dealt that heals the attacker. Only within the attacker's entity_interaction_range. Linear.")
    public AttributeSpec lifesteal = new AttributeSpec(0.0, 0.0, 1.0, StackingMode.LINEAR, 0.0, 1.0);

    @Comment("Armor-style mitigation against #c:is_magic damage. Works like the ARMOR attribute (toughness 0). Linear.")
    public AttributeSpec magicDefense = new AttributeSpec(0.0, 0.0, 30.0, StackingMode.LINEAR, 0.0, 1.0);

    @Comment("Health regenerated per second on any living entity below max health (no hunger or delay gating). Linear.")
    public AttributeSpec healthRegeneration = new AttributeSpec(0.0, 0.0, 10000.0, StackingMode.LINEAR, 0.0, 1.0);

    // --- Bow physics ---

    @Comment("Bow draw and crossbow charge speed bonus. +0.5 = 50% faster. Linear.")
    public AttributeSpec drawSpeed = new AttributeSpec(0.0, 0.0, 5.0, StackingMode.LINEAR, 0.0, 1.0);

    @Comment("Arrow velocity bonus for bows and crossbows. +0.5 = 50% faster. Also raises direct-hit damage, since arrow damage scales with speed. Linear.")
    public AttributeSpec arrowVelocity = new AttributeSpec(0.0, 0.0, 1_000_000.0, StackingMode.LINEAR, 0.0, 1.0);

    @Comment("Reduces arrow spread for bows and crossbows. +1.0 = perfect accuracy. Diminishing; full accuracy reachable but expensive.")
    public AttributeSpec accuracy = new AttributeSpec(0.0, 0.0, 1.0, StackingMode.PROBABILISTIC, 1.0, 2.0);

    // --- Player resources (stamina / mana) ---

    @Comment("Max stamina. HUD bar always shows 10 pips, scaled to this value. Player-only. Linear.")
    public AttributeSpec maxStamina = new AttributeSpec(20.0, 0.0, 10000.0, StackingMode.LINEAR, 0.0, 1.0);

    @Comment("Stamina regenerated per second. Player-only. Linear.")
    public AttributeSpec staminaRegen = new AttributeSpec(2.0, 0.0, 10000.0, StackingMode.LINEAR, 0.0, 1.0);

    @Comment("Multiplier on ability stamina costs. 1.0 = full, 0.5 = half, 0.0 = free; lower is better. Percent display. Player-only. Reductions diminish (~30% per-source cap) and stack multiplicatively.")
    public AttributeSpec staminaCost = new AttributeSpec(1.0, 0.0, 100.0, StackingMode.MULTIPLICATIVE, 0.3, 0.15);

    @Comment("Max mana. HUD bar always shows 10 pips, scaled to this value. Player-only. Linear.")
    public AttributeSpec maxMana = new AttributeSpec(20.0, 0.0, 10000.0, StackingMode.LINEAR, 0.0, 1.0);

    @Comment("Mana regenerated per second. Player-only. Linear.")
    public AttributeSpec manaRegen = new AttributeSpec(1.0, 0.0, 10000.0, StackingMode.LINEAR, 0.0, 1.0);

    @Comment("Multiplier on ability mana costs. 1.0 = full, 0.5 = half, 0.0 = free; lower is better. Percent display. Player-only. Reductions diminish (~30% per-source cap) and stack multiplicatively.")
    public AttributeSpec manaCost = new AttributeSpec(1.0, 0.0, 100.0, StackingMode.MULTIPLICATIVE, 0.3, 0.15);

    // --- Player progression ---

    @Comment("Multiplier on XP gained from orbs. 1.0 = vanilla, 0.5 = half, 2.0 = double. Affects pickup only, not orb values. Percent display. Player-only. Linear.")
    public AttributeSpec experienceGain = new AttributeSpec(1.0, 0.0, 100.0, StackingMode.LINEAR, 0.0, 1.0);
}
