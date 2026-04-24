package dev.muon.combat_attributes;

import net.fabricmc.api.ModInitializer;

public class CombatAttributesFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        
        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        CombatAttributes.LOG.info("Hello Fabric world!");
        CombatAttributes.init();
    }
}
