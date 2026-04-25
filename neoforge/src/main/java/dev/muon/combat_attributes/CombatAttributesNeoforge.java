package dev.muon.combat_attributes;

import dev.muon.combat_attributes.attribute.ModAttributesNeoforge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(CombatAttributes.MOD_ID)
public class CombatAttributesNeoforge {

    public CombatAttributesNeoforge(IEventBus eventBus) {
        // Common init runs first — registers FzzyConfig configs at the top of init(),
        // which the attribute constructors read from for default/min/max bounds.
        CombatAttributes.init();

        ModAttributesNeoforge.REGISTRY.register(eventBus);
        ModAttributesNeoforge.init();
    }
}
