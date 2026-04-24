package dev.muon.combat_attributes;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(CombatAttributes.MOD_ID)
public class CombatAttributesNeoforge {

    public CombatAttributesNeoforge(IEventBus eventBus) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        CombatAttributes.LOG.info("Hello NeoForge world!");
        CombatAttributes.init();

    }
}