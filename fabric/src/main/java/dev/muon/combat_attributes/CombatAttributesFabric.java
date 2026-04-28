package dev.muon.combat_attributes;

import dev.muon.combat_attributes.attribute.ModAttributesFabric;
import net.fabricmc.api.ModInitializer;

public class CombatAttributesFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        CombatAttributes.init();
        // ModAttributesFabric registers from <clinit>; touching the class via this
        // no-op forces it. DefaultAttributesMixin calls augment(), which also
        // forces <clinit>, as a defensive trampoline — idempotent because the JVM
        // runs <clinit> once.
        ModAttributesFabric.ensureInitialized();
    }
}
