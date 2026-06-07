package dev.muon.combat_attributes.compat;

import dev.muon.combat_attributes.CombatAttributes;
import dev.muon.combat_attributes.attribute.ModAttributes;
import dev.muon.dynamictooltips.api.DynamicTooltipsAPI;
import net.minecraft.resources.Identifier;

public final class DynamicTooltipsIntegration {

    private DynamicTooltipsIntegration() {}

    public static void init() {
        for (ModAttributes.Entry entry : ModAttributes.ALL) {
            // percent flag is read off ALL, independent of registration order, so init() is safe before or after holders are populated
            entry.percentScale().ifPresent(scale -> {
                Identifier id = Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, entry.id());
                DynamicTooltipsAPI.declarePercentAttribute(id, scale);
            });
        }
    }
}
