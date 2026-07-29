package dev.muon.combat_attributes;

import dev.muon.combat_attributes.attribute.ModAttributesNeoforge;
import dev.muon.combat_attributes.compat.AppleSkinHeartTooltip;
import dev.muon.combat_attributes.compat.AppleSkinIntegrationNeoforge;
import dev.muon.combat_attributes.platform.Services;
import dev.muon.combat_attributes.resource.PlayerResourceAttachmentNeoforge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(CombatAttributes.MOD_ID)
public class CombatAttributesNeoforge {

    public CombatAttributesNeoforge(IEventBus eventBus) {
        // Common init runs first; it registers FzzyConfig configs at the top of init(),
        // which the attribute constructors read for default/min/max bounds.
        CombatAttributes.init();

        ModAttributesNeoforge.REGISTRY.register(eventBus);
        PlayerResourceAttachmentNeoforge.REGISTRY.register(eventBus);
        ModAttributesNeoforge.init();

        if (FMLEnvironment.getDist() == Dist.CLIENT && Services.PLATFORM.isModLoaded(AppleSkinHeartTooltip.MOD_ID)) {
            AppleSkinIntegrationNeoforge.init();
        }
    }
}
