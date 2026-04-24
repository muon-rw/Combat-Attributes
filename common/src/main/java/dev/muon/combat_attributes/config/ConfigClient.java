package dev.muon.combat_attributes.config;

import dev.muon.combat_attributes.CombatAttributes;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedColor;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedEnum;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import net.minecraft.resources.Identifier;

/**
 * Client-only configuration. Loaded on the client and never synced from the server.
 *
 * <p>Use this for settings that only affect the local client — rendering preferences,
 * HUD toggles, key-binding alternatives, personal UI choices. Values set here are
 * authoritative on the player's own machine.
 *
 * <p>File: <code>config/&lt;modid&gt;/&lt;modid&gt;-client.toml</code>
 *
 * @see ConfigServer for server-only, non-synced settings
 * @see ConfigSync   for server-authoritative settings that sync to clients
 */
public class ConfigClient extends Config {

    public ConfigClient() {
        super(Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, "client"));
    }

    // --- Primitive toggles / numbers ---

    @Comment("Renders a small debug overlay with mod-internal state. Client-side only.")
    public ValidatedBoolean showDebugOverlay = new ValidatedBoolean(false);

    @Comment("Scale factor applied to the mod's HUD elements. 1.0 = vanilla size.")
    public ValidatedDouble hudScale = new ValidatedDouble(1.0, 2.0, 0.5);

    // --- Enum dropdown ---
    // ValidatedEnum generates a searchable popup / cycling button for any enum type.

    @Comment("Which screen corner the HUD anchors to.")
    public ValidatedEnum<HudCorner> hudCorner = new ValidatedEnum<>(HudCorner.TOP_LEFT);

    // --- Color picker ---
    // Opens a full RGB+hex popup. Use the transparent-enabled constructor to add an alpha channel.

    @Comment("Accent color for mod UI elements. RGB only — no alpha channel.")
    public ValidatedColor accentColor = new ValidatedColor(0x4A, 0x9E, 0xFF);

    /** Positions the HUD can be anchored to. */
    public enum HudCorner {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }
}
