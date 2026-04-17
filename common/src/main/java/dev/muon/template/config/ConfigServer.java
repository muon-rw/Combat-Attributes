package dev.muon.template.config;

import dev.muon.template.Template;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.util.TriState;
import me.fzzyhmstrs.fzzy_config.validation.collection.ValidatedList;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedTriState;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * Server-only configuration. Loaded on the logical server and never sent to clients.
 *
 * <p>Use this for operator/admin settings whose values the client never needs to know:
 * moderation thresholds, anti-abuse knobs, server-side logging levels, etc. On a
 * dedicated server this lives on the host; on a single-player integrated server it
 * lives on the player's machine.
 *
 * <p>File: <code>config/&lt;modid&gt;/&lt;modid&gt;-server.toml</code>
 *
 * @see ConfigClient for client-only, non-synced settings
 * @see ConfigSync   for server-authoritative settings that sync to clients
 */
public class ConfigServer extends Config {

    public ConfigServer() {
        super(Identifier.fromNamespaceAndPath(Template.MOD_ID, "server"));
    }

    // --- Primitive toggles / numbers ---

    @Comment("Emit verbose server-side debug logging for this mod.")
    public ValidatedBoolean verboseLogging = new ValidatedBoolean(false);

    @Comment("Maximum number of times a player may trigger an example action per minute.")
    public ValidatedInt rateLimitPerMinute = new ValidatedInt(30, 600, 0);

    // --- Tri-state (true / false / default-from-elsewhere) ---
    // Useful when you want to distinguish "explicitly off" from "inherit default".

    @Comment("Allow experimental features. DEFAULT defers to the mod's built-in policy.")
    public ValidatedTriState allowExperimentalFeatures = new ValidatedTriState(TriState.DEFAULT);

    // --- Collections ---
    // Static factories exist for lists of common primitive types (ofInt, ofDouble, ofString...).
    // The returned ValidatedList implements java.util.List, so reads behave like any List.

    @Comment("Item IDs blocked from the example action. Full resource paths, e.g. 'minecraft:tnt'.")
    public ValidatedList<String> bannedItemIds = ValidatedList.ofString(List.of(
            "minecraft:bedrock",
            "minecraft:command_block"
    ));
}
