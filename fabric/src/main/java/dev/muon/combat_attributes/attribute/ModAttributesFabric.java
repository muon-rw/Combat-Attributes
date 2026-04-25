package dev.muon.combat_attributes.attribute;

import dev.muon.combat_attributes.CombatAttributes;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

/**
 * Fabric-side attribute registration. Drives off {@link ModAttributes#ALL} so the
 * source of truth stays in common.
 *
 * <p>Reads {@code AttributeSpec.diminishing} per entry and registers either a
 * {@link DiminishingRangedAttribute} or vanilla {@link RangedAttribute}. Vanilla
 * stacking semantics (notably the compounding-per-modifier behavior of
 * ADD_MULTIPLIED_TOTAL) are preserved exactly when {@code diminishing=false}.
 *
 * <p>Must be invoked AFTER {@code Configs.register()} — both attribute constructors
 * read default/min/max/diminishing from the {@link AttributeSpec} synchronously.
 */
public final class ModAttributesFabric {

    private ModAttributesFabric() {}

    public static void init() {
        for (ModAttributes.Entry entry : ModAttributes.ALL) {
            String descriptionId = "attribute." + CombatAttributes.MOD_ID + "." + entry.id();
            AttributeSpec snap = entry.spec().get();
            Attribute attribute = snap.diminishing.get()
                    ? new DiminishingRangedAttribute(descriptionId, entry.spec())
                    : new RangedAttribute(descriptionId,
                            snap.defaultValue.get(), snap.minValue.get(), snap.maxValue.get());
            attribute.setSyncable(true);
            Holder<Attribute> holder = Registry.registerForHolder(
                    BuiltInRegistries.ATTRIBUTE,
                    Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, entry.id()),
                    attribute
            );
            ModAttributes.put(entry.id(), holder);
        }
    }
}
