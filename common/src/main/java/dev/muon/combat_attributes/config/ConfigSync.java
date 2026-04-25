package dev.muon.combat_attributes.config;

import dev.muon.combat_attributes.CombatAttributes;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
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
 * @see ConfigClient    for client-only, non-synced settings
 * @see ConfigServer    for server-only, non-synced settings
 */
public class ConfigSync extends Config {

    public ConfigSync() {
        super(Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, "sync"));
    }

    @Comment("If true, vanilla's jump-crit damage bonus is suppressed so it doesn't stack with this mod's Melee Crit attribute. " +
            "Leave false to keep both systems active (note: damage may double-crit when a jump-crit and a Melee Crit roll line up).")
    public ValidatedBoolean disableVanillaJumpCrits = new ValidatedBoolean(true);
}
