package dev.muon.combat_attributes;

import dev.muon.combat_attributes.attribute.ModAttributesFabric;
import net.fabricmc.api.ModInitializer;

public class CombatAttributesFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        // Common init runs first — registers FzzyConfig configs at the top of init(),
        // which the attribute constructors read from for default/min/max bounds.
        CombatAttributes.init();
        ModAttributesFabric.init();
    }
}
