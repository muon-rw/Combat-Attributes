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
 * {@link TomlHeaderComment} annotations below — they're rendered into the generated TOML so
 * pack authors can read them inline while editing.
 *
 * <p>File: <code>config/combat_attributes/combat_attributes-attributes.toml</code>
 */
@TomlHeaderComment(text = "===== Combat Attributes =====")
@TomlHeaderComment(text = "Per-attribute config: default value, bounds, stacking mode, and soft-cap shape.")
@TomlHeaderComment(text = "")
@TomlHeaderComment(text = "--- Stacking modes ---")
@TomlHeaderComment(text = "LINEAR        : vanilla math; M and k are inert.")
@TomlHeaderComment(text = "SOFT_CAP      : each operation slot diminishes additively. base + slot1 + slot2 + slot3.")
@TomlHeaderComment(text = "                Use for additive bonuses like crit damage.")
@TomlHeaderComment(text = "PROBABILISTIC : each operation slot diminishes, then slots combine via probabilistic")
@TomlHeaderComment(text = "                union: 1 - (1-base)(1-slot1)(1-slot2)(1-slot3). Two 40% sources -> 64%,")
@TomlHeaderComment(text = "                three -> 78.4%, asymptote 100%. Use for chance attributes.")
@TomlHeaderComment(text = "MULTIPLICATIVE: inverse-sign analog — REDUCTIONS (negative modifier sums) diminish per")
@TomlHeaderComment(text = "                slot, then slots combine multiplicatively as cost factors:")
@TomlHeaderComment(text = "                base * (1-r1) * (1-r2) * (1-r3). Two 30% reductions -> 51% off.")
@TomlHeaderComment(text = "                Increases (positive sums) pass through linearly as (1+x). Use for")
@TomlHeaderComment(text = "                'lower is better' multipliers like mana cost.")
@TomlHeaderComment(text = "")
@TomlHeaderComment(text = "Each modifier operation (ADD_VALUE / ADD_MULTIPLIED_BASE / ADD_MULTIPLIED_TOTAL) is")
@TomlHeaderComment(text = "treated as one independent 'source' whose modifier amounts sum, then diminish to that")
@TomlHeaderComment(text = "slot's per-source cap. Addons cannot bypass the cap by using a different operation.")
@TomlHeaderComment(text = "")
@TomlHeaderComment(text = "--- Tuning M (softCap) and k (halfSaturation) ---")
@TomlHeaderComment(text = "Non-linear modes apply: min(x, M*x/(x+k))")
@TomlHeaderComment(text = "  M = the ceiling. The most a single source can ever contribute. For chance attributes")
@TomlHeaderComment(text = "      this is the per-source probability cap (e.g. 0.4 = 40%). For multiplier attributes")
@TomlHeaderComment(text = "      like crit damage, the cap is added on top of the base value (M=5 above base=1.5 ->")
@TomlHeaderComment(text = "      max 6.5x from a single source).")
@TomlHeaderComment(text = "  k = the softness of that ceiling. Modifiers below (M - k) pass through linearly. Above")
@TomlHeaderComment(text = "      that, the curve takes over and asymptotes to M. Small k = nearly hard cap with a")
@TomlHeaderComment(text = "      tiny soft buffer. Large k = early diminishing with a long tail toward M.")
@TomlHeaderComment(text = "")
@TomlHeaderComment(text = "Quick intuition with M = 0.4 (40% cap):")
@TomlHeaderComment(text = "  k = 0.001 -> linear up to 39.9%, then a sliver of curve to 40%. Practically hard cap.")
@TomlHeaderComment(text = "  k = 0.05  -> linear up to 35%, then 5%-wide curve to 40%. (current crit chance)")
@TomlHeaderComment(text = "  k = 0.2   -> linear up to 20%, then a long, gentle climb from 20% toward 40%.")
@TomlHeaderComment(text = "  k >= M    -> no linear region; the curve diminishes from the very first modifier.")
@TomlHeaderComment(text = "")
@TomlHeaderComment(text = "Rule of thumb: pick k by deciding what fraction of M should be reachable linearly.")
@TomlHeaderComment(text = "  90% of M reachable linearly -> k = M * 0.1")
@TomlHeaderComment(text = "  70% of M reachable linearly -> k = M * 0.3")
@TomlHeaderComment(text = "")
@TomlHeaderComment(text = "--- Default calibration ---")
@TomlHeaderComment(text = "Crit chance   : M=0.4, k=0.05. ~36% at 50 stat points (0.01/pt). Two sources -> 64%.")
@TomlHeaderComment(text = "Evasion       : M=0.3, k=0.05. Lower cap; two maxed sources still leave ~51% exposed.")
@TomlHeaderComment(text = "Crit damage   : M=5,   k=2.   Additive above the 1.5 base. 6.5x max from one source.")
@TomlHeaderComment(text = "Accuracy      : M=1.0, k=2.0. Full accuracy reachable but expensive (no linear region).")
@TomlHeaderComment(text = "Ranged damage, lifesteal, draw speed, arrow velocity: LINEAR (vanilla stacking).")
@Version(version = 1)
public class ConfigAttributes extends Config {

    public ConfigAttributes() {
        super(Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, "attributes"));
    }

    // --- Melee ---

    @Comment("Probability (0–1) of a melee critical strike on attack. PROBABILISTIC stacking — caps at 50% per source; multiple sources combine via probabilistic union.")
    public AttributeSpec meleeCritChance = new AttributeSpec(0.0, 0.0, 1.0, StackingMode.PROBABILISTIC, 0.5, 0.15);

    @Comment("Damage multiplier applied to melee critical strikes. Default 1.5 (matches vanilla jump-crit). SOFT_CAP — each operation adds up to +5 above base, asymptote ~6.5× from a single source.")
    public AttributeSpec meleeCritDamage = new AttributeSpec(1.5, 1.0, 100.0, StackingMode.SOFT_CAP, 1, 0.4);

    // --- Ranged ---

    @Comment("Flat damage bonus added to non-magic projectile damage sources (excludes #c:is_magic). LINEAR — vanilla stacking.")
    public AttributeSpec rangedDamage = new AttributeSpec(0.0, -100.0, 1000.0, StackingMode.LINEAR, 0.0, 1.0);

    @Comment("Probability (0–1) of a ranged (projectile) critical strike. Separate roll from melee/magic. PROBABILISTIC — 50% per-source cap.")
    public AttributeSpec rangedCritChance = new AttributeSpec(0.0, 0.0, 1.0, StackingMode.PROBABILISTIC, 0.5, 0.15);

    @Comment("Damage multiplier applied to ranged critical strikes. Default 1.5. SOFT_CAP — same shape as melee crit damage.")
    public AttributeSpec rangedCritDamage = new AttributeSpec(1.5, 1.0, 100.0, StackingMode.SOFT_CAP, 1.0, 0.4);

    // --- Magic ---

    @Comment("Probability (0–1) of a magic critical strike on damage tagged #c:is_magic. Separate roll. PROBABILISTIC — 50% per-source cap.")
    public AttributeSpec magicCritChance = new AttributeSpec(0.0, 0.0, 1.0, StackingMode.PROBABILISTIC, 0.5, 0.15);

    @Comment("Damage multiplier applied to magic critical strikes. Default 1.5. SOFT_CAP — same shape as melee crit damage.")
    public AttributeSpec magicCritDamage = new AttributeSpec(1.5, 1.0, 100.0, StackingMode.SOFT_CAP, 1.0, 0.4);

    @Comment("Magic damage multiplier — analog to attack damage for magic. Base 1.0. Currently has no built-in hooks; reserved for spell mods to read. LINEAR — vanilla stacking.")
    public AttributeSpec magicPower = new AttributeSpec(1.0, 0.0, 1000.0, StackingMode.LINEAR, 0.0, 1.0);

    // --- Defensive ---

    @Comment("Probability (0–1) of dodging incoming damage of any source. PROBABILISTIC — capped at 30% per source, lower than crit chance so two maxed sources still leave the player ~50% exposed.")
    public AttributeSpec evasion = new AttributeSpec(0.0, 0.0, 1.0, StackingMode.PROBABILISTIC, 0.3, 0.15);

    @Comment("Fraction of damage dealt that heals the attacker. Applies only when the attacker is within their entity_interaction_range of the victim. LINEAR — vanilla stacking.")
    public AttributeSpec lifesteal = new AttributeSpec(0.0, 0.0, 1.0, StackingMode.LINEAR, 0.0, 1.0);

    @Comment("Armor-style mitigation that applies to incoming #c:is_magic damage. Uses vanilla's CombatRules.getDamageAfterAbsorb formula with toughness=0 — same shape as the ARMOR attribute. LINEAR — vanilla stacking.")
    public AttributeSpec magicDefense = new AttributeSpec(0.0, 0.0, 30.0, StackingMode.LINEAR, 0.0, 1.0);

    // --- Bow physics ---

    @Comment("Bonus to bow draw speed and crossbow charge speed. +0.5 = 50% faster (held time effectively scaled by 1.5; crossbow charge duration divided by 1.5). LINEAR — vanilla stacking.")
    public AttributeSpec drawSpeed = new AttributeSpec(0.0, 0.0, 5.0, StackingMode.LINEAR, 0.0, 1.0);

    @Comment("Bonus to fired arrow velocity for both bows and crossbows. +0.5 = 50% faster arrow. LINEAR — vanilla stacking. Note: AbstractArrow base damage scales with velocity, so this also indirectly increases hit damage on direct projectiles. Effectively unclamped (max=1_000_000).")
    public AttributeSpec arrowVelocity = new AttributeSpec(0.0, 0.0, 1_000_000.0, StackingMode.LINEAR, 0.0, 1.0);

    @Comment("Reduces fired arrow inaccuracy for both bows and crossbows. +1.0 = perfect accuracy (no spread). PROBABILISTIC stacking with M=1.0 and a slow k=2.0 — full accuracy reachable but expensive.")
    public AttributeSpec accuracy = new AttributeSpec(0.0, 0.0, 1.0, StackingMode.PROBABILISTIC, 1.0, 2.0);

    // --- Player resources (stamina / mana) ---

    @Comment("Maximum stamina pool. The HUD bar always shows 10 pips, scaled to this value. Player-only. LINEAR — vanilla stacking.")
    public AttributeSpec maxStamina = new AttributeSpec(20.0, 0.0, 10000.0, StackingMode.LINEAR, 0.0, 1.0);

    @Comment("Stamina regenerated per second (split across 20 ticks). Player-only. LINEAR — vanilla stacking.")
    public AttributeSpec staminaRegen = new AttributeSpec(2.0, 0.0, 10000.0, StackingMode.LINEAR, 0.0, 1.0);

    @Comment("Multiplier on stamina costs paid by abilities — 1.0 = full cost, 0.5 = half cost, 0.0 = free. Other mods are expected to consume current stamina via this multiplier. Percent display, NEGATIVE sentiment (lower is better). Player-only. MULTIPLICATIVE stacking — reductions diminish per slot at a 30% cap, then combine multiplicatively (two 30% reductions → 51% off), mirroring evasion's shape on the buff side.")
    public AttributeSpec staminaCost = new AttributeSpec(1.0, 0.0, 100.0, StackingMode.MULTIPLICATIVE, 0.3, 0.15);

    @Comment("Maximum mana pool. The HUD bar always shows 10 pips, scaled to this value. Player-only. LINEAR — vanilla stacking.")
    public AttributeSpec maxMana = new AttributeSpec(20.0, 0.0, 10000.0, StackingMode.LINEAR, 0.0, 1.0);

    @Comment("Mana regenerated per second (split across 20 ticks). Player-only. LINEAR — vanilla stacking.")
    public AttributeSpec manaRegen = new AttributeSpec(1.0, 0.0, 10000.0, StackingMode.LINEAR, 0.0, 1.0);

    @Comment("Multiplier on mana costs paid by abilities — 1.0 = full cost, 0.5 = half cost, 0.0 = free. Other mods are expected to consume current mana via this multiplier. Percent display, NEGATIVE sentiment (lower is better). Player-only. MULTIPLICATIVE stacking — reductions diminish per slot at a 30% cap, then combine multiplicatively (two 30% reductions → 51% off), mirroring evasion's shape on the buff side.")
    public AttributeSpec manaCost = new AttributeSpec(1.0, 0.0, 100.0, StackingMode.MULTIPLICATIVE, 0.3, 0.15);

    // --- Player progression ---

    @Comment("Multiplier on XP added to the player's XP bar from experience orbs — 1.0 = vanilla, 0.5 = half, 2.0 = double. Below 1.0 reduces gain. Does not change orb values themselves; only what's awarded on pickup. Percent display. Player-only. LINEAR — vanilla stacking.")
    public AttributeSpec experienceGain = new AttributeSpec(1.0, 0.0, 100.0, StackingMode.LINEAR, 0.0, 1.0);
}
