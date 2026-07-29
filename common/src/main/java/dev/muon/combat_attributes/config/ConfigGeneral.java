package dev.muon.combat_attributes.config;

import dev.muon.combat_attributes.CombatAttributes;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import net.minecraft.resources.Identifier;

/**
 * Server-authoritative configuration, synced from server to connected clients.
 *
 * <p>The server's values win: whatever the player has locally gets overwritten
 * on join, and server-side updates propagate live.
 *
 * <p>File: <code>config/combat_attributes/combat_attributes-sync.toml</code>
 *
 * @see ConfigAttributes for per-attribute defaults / bounds / formulas
 */
public class ConfigGeneral extends Config {

    public ConfigGeneral() {
        super(Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, "general"));
    }

    @Comment("Suppress vanilla jump-crit damage so it doesn't stack with the Melee Crit attribute. " +
            "False keeps both (can double-crit when a jump-crit and Melee Crit line up).")
    public ValidatedBoolean disableVanillaJumpCrits = new ValidatedBoolean(true);

    @Comment("Hide the stamina HUD bar at full stamina. The oxygen bar shifts down to fill the gap.")
    public ValidatedBoolean hideStaminaWhenFull = new ValidatedBoolean(true);

    @Comment("Hide the mana HUD bar at full mana. The oxygen bar shifts down to fill the gap.")
    public ValidatedBoolean hideManaWhenFull = new ValidatedBoolean(true);

    @Comment("Replace vanilla hunger with direct healing on eating (Legacy-style). Hunger bar hidden, " +
            "any food eaten heals based on its nutrition/saturation (see legacyHungerHealPer* options). " +
            "No exhaustion, starvation, or vanilla regen, and food is always eatable.")
    public ValidatedBoolean legacyHunger = new ValidatedBoolean(true);

    @Comment("Half-hearts healed per point of food nutrition when legacyHunger is on. " +
            "Bread (nutrition 5) at 0.5 = 2.5 half-hearts from this term.")
    public ValidatedDouble legacyHungerHealPerNutrition = new ValidatedDouble(0.5, 100.0, 0.0);

    @Comment("Half-hearts healed per point of food saturation when legacyHunger is on. " +
            "Bread (saturation 6.0) at 0.25 = 1.5 half-hearts from this term.")
    public ValidatedDouble legacyHungerHealPerSaturation = new ValidatedDouble(0.25, 100.0, 0.0);

    @Comment("Stamina drained per landed melee hit (full-cooldown swings only). " +
            "Scaled by the stamina_cost attribute. Set 0 to disable.")
    public ValidatedDouble attackStaminaCost = new ValidatedDouble(1.0, 1000.0, 0.0);

    @Comment("Stamina drained per block broken. Flat cost, not scaled by hardness or tool. " +
            "When exhausted, breaking slows (exhaustedBreakSpeedMultiplier) instead of stopping. " +
            "Scaled by the stamina_cost attribute. Set 0 to disable.")
    public ValidatedDouble blockBreakStaminaCost = new ValidatedDouble(0.1, 1000.0, 0.0);

    @Comment("Block destroy speed while stamina is empty (0.3 = Mining Fatigue I severity). " +
            "Set 1.0 to disable the slowdown. Stacks with vanilla mining fatigue.")
    public ValidatedDouble exhaustedBreakSpeedMultiplier = new ValidatedDouble(0.3, 1.0, 0.0);

    @Comment("Stamina drained per tick while sprinting. When empty, sprint is forced off until regen resumes. " +
            "Set 0 to disable.")
    public ValidatedDouble sprintStaminaCost = new ValidatedDouble(0.05, 1000.0, 0.0);

    @Comment("Stamina drained per jump. Jumping is blocked when empty. Default 0 (disabled).")
    public ValidatedDouble jumpStaminaCost = new ValidatedDouble(0.0, 1000.0, 0.0);

    @Comment("Stamina drained over a full bow/crossbow/ranged-weapon charge, spread across the draw " +
            "(half-charge pays ~half). Drawing stops when empty. Tridents use tridentThrowStaminaCost. " +
            "Set 0 to disable.")
    public ValidatedDouble rangedDrawStaminaCost = new ValidatedDouble(1.0, 1000.0, 0.0);

    @Comment("Pause stamina regen while drawing a bow/crossbow/ranged weapon, so the draw drain isn't offset. " +
            "Charge drain (rangedDrawStaminaCost) still applies. Tridents not covered.")
    public ValidatedBoolean rangedDrawPausesStaminaRegen = new ValidatedBoolean(true);

    @Comment("Pause stamina regen while the player's head is underwater. Player must surface to recover. " +
            "Stacks with swim drain.")
    public ValidatedBoolean underwaterPausesStaminaRegen = new ValidatedBoolean(true);

    @Comment("Seconds of regen delay after any stamina drain (Souls-style recovery window). " +
            "Continuous drains keep re-arming it, so regen resumes this long after you stop. " +
            "Set 0 to disable.")
    public ValidatedDouble staminaDrainRegenDelay = new ValidatedDouble(0.6, 60.0, 0.0);

    @Comment("Stamina drained on a successful trident throw (riptide and regular both count). " +
            "Failed/blocked throws don't drain. Set 0 to disable.")
    public ValidatedDouble tridentThrowStaminaCost = new ValidatedDouble(2.0, 1000.0, 0.0);

    @Comment("Stamina drained per tick while actively swimming. Swimming is forced off when empty. " +
            "Set 0 to disable.")
    public ValidatedDouble swimStaminaCost = new ValidatedDouble(0.05, 1000.0, 0.0);

    @Comment("Stamina drained per tick while elytra-gliding. When empty, glide drops and the player falls. " +
            "Set 0 to disable.")
    public ValidatedDouble elytraStaminaCost = new ValidatedDouble(0.1, 1000.0, 0.0);

    @Comment("Extra seconds of regen lockout when stamina hits zero (heavier exhaustion penalty). " +
            "During it, regen and all stamina actions are blocked; mana is unaffected. " +
            "Set 0 to disable (staminaDrainRegenDelay still applies).")
    public ValidatedDouble staminaEmptyRegenDelay = new ValidatedDouble(1.6, 60.0, 0.0);
}
