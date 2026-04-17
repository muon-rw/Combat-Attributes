package dev.muon.template;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Template.MOD_ID)
public class TemplateNeoforge {

    public TemplateNeoforge(IEventBus eventBus) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        Template.LOG.info("Hello NeoForge world!");
        Template.init();

    }
}