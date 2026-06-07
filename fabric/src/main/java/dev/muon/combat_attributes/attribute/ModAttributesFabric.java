package dev.muon.combat_attributes.attribute;

import dev.muon.combat_attributes.CombatAttributes;
import dev.muon.combat_attributes.config.Configs;
import dev.muon.combat_attributes.mixin.AttributeSupplierAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.Map;

public final class ModAttributesFabric {

    static {
        // Configs first: attribute constructors read default/min/max/stackingMode
        // synchronously off the AttributeSpec. Configs.register() is idempotent.
        Configs.register();
        registerAll();
    }

    private ModAttributesFabric() {}

    /** No-op call that forces this class's {@code <clinit>}, registering every mod attribute once. */
    public static void init() {}

    /**
     * Copies {@code original} plus every Combat Attributes holder for {@code entityType}
     * ({@code playerOnly} entries only for the player), reading its instances via
     * {@link AttributeSupplierAccessor} since vanilla's {@code AttributeSupplier.Builder} has no
     * copy constructor.
     */
    public static AttributeSupplier augment(AttributeSupplier original, EntityType<?> entityType) {
        init();
        boolean isPlayer = entityType == EntityType.PLAYER;
        AttributeSupplier.Builder builder = AttributeSupplier.builder();
        Map<Holder<Attribute>, AttributeInstance> instances =
                ((AttributeSupplierAccessor) original).combat_attributes$getInstances();
        for (Map.Entry<Holder<Attribute>, AttributeInstance> entry : instances.entrySet()) {
            builder.add(entry.getKey(), entry.getValue().getBaseValue());
        }
        for (ModAttributes.Entry entry : ModAttributes.ALL) {
            if (entry.playerOnly() && !isPlayer) continue;
            Holder<Attribute> holder = ModAttributes.get(entry.id());
            if (!original.hasAttribute(holder)) {
                builder.add(holder);
            }
        }
        return builder.build();
    }

    private static void registerAll() {
        for (ModAttributes.Entry entry : ModAttributes.ALL) {
            String descriptionId = "attribute." + CombatAttributes.MOD_ID + "." + entry.id();
            AttributeSpec spec = entry.spec().get();
            Attribute attribute = spec.stackingMode.get() != AttributeSpec.StackingMode.LINEAR
                    ? new DiminishingRangedAttribute(descriptionId, entry.spec())
                    : new RangedAttribute(descriptionId,
                            spec.defaultValue.get(), spec.minValue.get(), spec.maxValue.get());
            attribute.setSyncable(true);
            attribute.setSentiment(entry.sentiment());
            Holder<Attribute> holder = Registry.registerForHolder(
                    BuiltInRegistries.ATTRIBUTE,
                    Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, entry.id()),
                    attribute
            );
            ModAttributes.put(entry.id(), holder);
        }
    }
}
