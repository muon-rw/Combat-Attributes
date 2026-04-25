package dev.muon.combat_attributes.attribute;

import dev.muon.combat_attributes.CombatAttributes;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;

/**
 * Fabric-side attribute registration. Drives off {@link ModAttributes#ALL} so the
 * source of truth stays in common.
 *
 * <p>Must be invoked AFTER {@code Configs.register()} — the
 * {@link DiminishingRangedAttribute} constructor reads default/min/max from the
 * {@link AttributeSpec} synchronously.
 */
public final class ModAttributesFabric {

    private ModAttributesFabric() {}

    public static void init() {
        for (ModAttributes.Entry entry : ModAttributes.ALL) {
            String descriptionId = "attribute." + CombatAttributes.MOD_ID + "." + entry.id();
            Attribute attribute = new DiminishingRangedAttribute(descriptionId, entry.spec()).setSyncable(true);
            Holder<Attribute> holder = Registry.registerForHolder(
                    BuiltInRegistries.ATTRIBUTE,
                    Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, entry.id()),
                    attribute
            );
            ModAttributes.put(entry.id(), holder);
        }
    }
}
