package dev.muon.combat_attributes.compat;

import dev.muon.combat_attributes.CombatAttributes;
import dev.muon.combat_attributes.attribute.ModAttributes;
import dev.muon.dynamictooltips.api.DynamicTooltipsAPI;
import net.minecraft.resources.Identifier;

/**
 * Registers our percent-display attributes with Dynamic Tooltips on Fabric. Mirrors the
 * NeoForge story (where percent-flagged attributes will eventually extend
 * {@code PercentageAttribute} directly). Users can countermand any registration from their
 * own DT config — the API merges with user config, with config taking precedence.
 *
 * <p>Must be invoked AFTER {@code ModAttributesFabric.init()} so the holder map is populated;
 * the percent flag itself comes off {@link ModAttributes#ALL}, which is independent of
 * registration order.
 */
public final class DynamicTooltipsIntegration {

    private DynamicTooltipsIntegration() {}

    public static void init() {
        for (ModAttributes.Entry entry : ModAttributes.ALL) {
            entry.percentScale().ifPresent(scale -> {
                Identifier id = Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, entry.id());
                DynamicTooltipsAPI.declarePercentAttribute(id, scale);
            });
        }
    }
}
