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
}
