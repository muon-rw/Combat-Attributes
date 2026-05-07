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

    @Comment("If true, vanilla's jump-crit damage bonus is suppressed so it doesn't stack with this mod's Melee Crit attribute. " +
            "Leave false to keep both systems active (note: damage may double-crit when a jump-crit and a Melee Crit roll line up).")
    public ValidatedBoolean disableVanillaJumpCrits = new ValidatedBoolean(true);

    @Comment("If true, the stamina HUD bar is hidden while at full stamina. The oxygen bar above shifts down to fill the gap.")
    public ValidatedBoolean hideStaminaWhenFull = new ValidatedBoolean(true);

    @Comment("If true, the mana HUD bar is hidden while at full mana. The oxygen bar above shifts down to fill the gap.")
    public ValidatedBoolean hideManaWhenFull = new ValidatedBoolean(true);

    @Comment("If true, replaces the vanilla hunger system with direct healing on food consumption. " +
            "The hunger bar is hidden (our stamina/mana bars shift down into its slot), eating any " +
            "food heals the player by legacyHungerHealPerNutrition * nutrition + " +
            "legacyHungerHealPerSaturation * saturation half-hearts (rounded), and the food field " +
            "is frozen in [7, 17] — no exhaustion drain, no starvation damage, no vanilla regen, " +
            "and any food stays eligible to consume regardless of hunger.")
    public ValidatedBoolean legacyHunger = new ValidatedBoolean(true);

    @Comment("Half-hearts of healing per point of food nutrition when legacyHunger is enabled. " +
            "Bread (nutrition=5) at 0.5 -> 2.5 half-hearts (1.25 hearts) from this term.")
    public ValidatedDouble legacyHungerHealPerNutrition = new ValidatedDouble(0.5, 100.0, 0.0);

    @Comment("Half-hearts of healing per point of food saturation when legacyHunger is enabled. " +
            "Bread (saturation=6.0) at 0.25 -> 1.5 half-hearts (0.75 hearts) from this term.")
    public ValidatedDouble legacyHungerHealPerSaturation = new ValidatedDouble(0.25, 100.0, 0.0);

    // --- Stamina consumers ---
    // Configured base costs are scaled by the player's stamina_cost attribute (default 1.0,
    // lower = cheaper) via a listener registered on ChangeStaminaEvent / ChangeStaminaCallback.
    // Set any cost to 0.0 to disable that consumer entirely. While stamina sits at zero
    // (the regen-delay lockout window), the corresponding action is blocked outright.

    @Comment("Stamina drained per landed melee hit (server-side, full-cooldown swings only — strength-scale >= 0.9). " +
            "Set to 0.0 to disable. Scaled by the stamina_cost attribute.")
    public ValidatedDouble attackStaminaCost = new ValidatedDouble(1.0, 1000.0, 0.0);

    @Comment("Stamina drained per block broken by the player (server-side). Vanilla's analogous food exhaustion " +
            "is a flat 0.005 per block; this is the same shape, scaled up to a stamina-bar-relevant value. Cost " +
            "is flat — not scaled by block hardness, tool, or mining time. The fast-mining penalty emerges from " +
            "the universal staminaDrainRegenDelay re-arming each break. When exhausted, the break still proceeds " +
            "but at exhaustedBreakSpeedMultiplier of normal speed (mining-fatigue style) rather than being " +
            "cancelled. Set to 0.0 to disable the drain entirely. Scaled by the stamina_cost attribute.")
    public ValidatedDouble blockBreakStaminaCost = new ValidatedDouble(0.1, 1000.0, 0.0);

    @Comment("Multiplier applied to the player's block destroy speed while stamina is at zero — the " +
            "exhaustion-as-mining-fatigue mechanic. Vanilla's MINING_FATIGUE I uses 0.3, II uses 0.09, " +
            "III uses 0.0027; this default of 0.3 lands at MINING_FATIGUE-I severity. Set to 1.0 to disable " +
            "the slowdown (breaks proceed at full speed even when exhausted). Stacks multiplicatively with " +
            "vanilla mining fatigue if both apply.")
    public ValidatedDouble exhaustedBreakSpeedMultiplier = new ValidatedDouble(0.3, 1.0, 0.0);

    @Comment("Stamina drained per server tick while sprinting. At default staminaRegen=1.0/sec, the break-even tick rate is 0.05. " +
            "When stamina hits zero the player is force-unsprinted until the lockout ends. Set to 0.0 to disable.")
    public ValidatedDouble sprintStaminaCost = new ValidatedDouble(0.05, 1000.0, 0.0);

    @Comment("Stamina drained per ground jump (Player#jumpFromGround). " +
            "Jumping is blocked when stamina is exhausted. Default 0.0 (disabled).")
    public ValidatedDouble jumpStaminaCost = new ValidatedDouble(0.0, 1000.0, 0.0);

    @Comment("Stamina drained over a full charge of any ProjectileWeaponItem — bow (20 ticks), crossbow " +
            "(getChargeDuration, quick_charge-aware), or mod ranged weapons (their getUseDuration). The cost " +
            "is spread linearly across the item's effective charge ticks, so a half-charge release pays " +
            "roughly half. Higher draw_speed accelerates both the charge AND the drain rate, keeping total " +
            "cost constant. Drawing is aborted (stopUsingItem) when stamina is exhausted. Set to 0.0 to disable. " +
            "Tridents are not covered — see tridentThrowStaminaCost.")
    public ValidatedDouble rangedDrawStaminaCost = new ValidatedDouble(1.0, 1000.0, 0.0);

    @Comment("If true, stamina regen is paused for as long as the player is using any ProjectileWeaponItem — " +
            "i.e. throughout a bow draw (charge + held at full), throughout a crossbow charge, and throughout " +
            "use of mod-defined ranged weapons. Charge-phase drain (rangedDrawStaminaCost) still applies; this " +
            "only suppresses the per-tick regen so the drain is not partially offset. Tridents are not covered. " +
            "Note: mod ranged weapons that extend ProjectileWeaponItem with vanilla's default 72000-tick " +
            "getUseDuration will pause regen for as long as right-click is held.")
    public ValidatedBoolean rangedDrawPausesStaminaRegen = new ValidatedBoolean(true);

    @Comment("If true, stamina regen is paused while the player's head is submerged (isUnderWater()). " +
            "Composes with the existing swim drain — actively swimming underwater both drains and pauses " +
            "regen; standing still underwater (e.g. on the floor) just pauses regen. Player must surface " +
            "to recover stamina.")
    public ValidatedBoolean underwaterPausesStaminaRegen = new ValidatedBoolean(true);

    @Comment("Seconds of stamina regen lockout armed automatically after every successful stamina drain " +
            "(attack, sprint tick, swim tick, elytra tick, jump, bow draw tick, trident throw, mod-driven " +
            "drains). Modeled after Souls-likes / For Honor: a brief recovery window between expenditure and " +
            "regen that gates combat tempo and prevents micro-drain exploits (e.g. spam-jumping while regen " +
            "ticks). Continuous drains keep re-arming each tick, so regen only resumes after the player " +
            "stops the action and waits this many seconds. Composed with the post-exhaustion lockout via " +
            "max() — exhaustion always wins. Set to 0.0 to disable.")
    public ValidatedDouble staminaDrainRegenDelay = new ValidatedDouble(0.6, 60.0, 0.0);

    @Comment("Stamina drained on a successful trident throw (timeHeld >= 10 ticks AND vanilla decided to " +
            "actually fire — broken tridents, failed riptide conditions etc. don't drain). Riptide and " +
            "regular throws both count. Set to 0.0 to disable.")
    public ValidatedDouble tridentThrowStaminaCost = new ValidatedDouble(2.0, 1000.0, 0.0);

    @Comment("Stamina drained per server tick while actively swimming (isSwimming() && isUnderWater()). " +
            "Player is force-unswum when stamina is exhausted. Set to 0.0 to disable.")
    public ValidatedDouble swimStaminaCost = new ValidatedDouble(0.05, 1000.0, 0.0);

    @Comment("Stamina drained per server tick while elytra-gliding (isFallFlying()). " +
            "Player is force-dropped from glide when stamina is exhausted — they will fall. Set to 0.0 to disable.")
    public ValidatedDouble elytraStaminaCost = new ValidatedDouble(0.1, 1000.0, 0.0);

    @Comment("Seconds of regen lockout when stamina hits zero — the heavier exhaustion penalty layered on top " +
            "of the universal staminaDrainRegenDelay (which arms on every drain). While this timer counts down, " +
            "stamina cannot regenerate and consumer actions (attack, sprint, jump, bow draw, swim, elytra) are " +
            "blocked. Mana is unaffected. Composed via max() — set this to less than staminaDrainRegenDelay if " +
            "you want exhaustion to behave the same as any other drain. Set to 0.0 to disable the heavier " +
            "exhaustion lockout (the universal delay still applies).")
    public ValidatedDouble staminaEmptyRegenDelay = new ValidatedDouble(1.6, 60.0, 0.0);
}
