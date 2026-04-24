package dev.muon.combat_attributes.config;

import dev.muon.combat_attributes.CombatAttributes;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.minecraft.ValidatedIdentifier;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedExpression;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;
import net.minecraft.resources.Identifier;

import java.util.Set;

/**
 * Server-authoritative configuration, synced from server to connected clients.
 *
 * <p>The server's values win: whatever the player has locally gets overwritten
 * on join, and server-side updates propagate live. Use this for any setting
 * both sides must agree on — gameplay values, feature flags, tuning constants
 * that influence both client prediction and server logic.
 *
 * <p>By default, modifying values requires Op level 2. Annotate individual fields
 * with {@link me.fzzyhmstrs.fzzy_config.annotations.WithPerms} to raise that
 * floor, or {@link me.fzzyhmstrs.fzzy_config.annotations.ClientModifiable} to
 * let any player change a local copy (cosmetic splits only).
 *
 * <p>File: <code>config/&lt;modid&gt;/&lt;modid&gt;-sync.toml</code>
 *
 * @see ConfigClient for client-only, non-synced settings
 * @see ConfigServer for server-only, non-synced settings
 */
public class ConfigSync extends Config {

    public ConfigSync() {
        super(Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, "sync"));
    }

    // --- Primitive toggles / numbers ---

    @Comment("Master switch for the example feature. Disabling hides it on every client.")
    public ValidatedBoolean featureEnabled = new ValidatedBoolean(true);

    @Comment("Multiplier applied to the example mechanic. Must match between client and server.")
    public ValidatedDouble strengthMultiplier = new ValidatedDouble(1.0, 10.0, 0.0);

    @Comment("Cooldown (in ticks, 20 = 1s) between triggers of the example mechanic.")
    public ValidatedInt cooldownTicks = new ValidatedInt(20, 1200, 0);

    // --- Math expression ---
    // Users enter a formula (e.g. "x*1.5 + 2"); evaluate with damageFormula.evalSafe(
    // Map.of('x', baseDamage), baseDamage). The GUI offers a formula keyboard popup.

    @Comment("Formula applied to incoming damage. 'x' is the base damage value.")
    public ValidatedExpression damageFormula = new ValidatedExpression("x * 1.0", Set.of('x'));

    // --- Minecraft Identifier ---
    // Validates against the resource-location grammar. Use ValidatedIdentifier.ofRegistry(...)
    // or ofTag(...) to restrict values to a specific registry/tag with autocomplete.

    @Comment("Default biome for the example structure. Any valid resource location.")
    public ValidatedIdentifier exampleBiome = new ValidatedIdentifier(
            Identifier.fromNamespaceAndPath("minecraft", "plains")
    );
}
