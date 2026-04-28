package dev.muon.combat_attributes.attribute;

import dev.muon.combat_attributes.CombatAttributes;
import dev.muon.combat_attributes.config.Configs;
import dev.muon.combat_attributes.mixin.AttributeSupplierAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.Map;

/**
 * Fabric-side attribute registration. Drives off {@link ModAttributes#ALL} so the
 * source of truth stays in common.
 *
 * <p>Registration runs from the class's static initializer. Both the
 * {@code DefaultAttributes#getSupplier} mixin and the
 * {@code ModInitializer.onInitialize} entrypoint reach registration through
 * {@link #ensureInitialized()}, which is a no-op call that exists only to force
 * {@code <clinit>}. Whichever runs first wins; subsequent calls are no-ops because
 * the JVM runs {@code <clinit>} exactly once. The mixin call is defensive —
 * {@code getSupplier} is invoked at entity-construction time, well after
 * {@code onInitialize}, so in practice the entrypoint path always populates the
 * holder map first.
 *
 * <p>Reads {@code AttributeSpec.diminishing} per entry and registers either a
 * {@link DiminishingRangedAttribute} or vanilla {@link RangedAttribute}. Vanilla
 * stacking semantics (notably the compounding-per-modifier behavior of
 * ADD_MULTIPLIED_TOTAL) are preserved exactly when {@code diminishing=false}.
 */
public final class ModAttributesFabric {

    static {
        // Configs first — attribute constructors read default/min/max/diminishing
        // synchronously off the AttributeSpec. Configs.register() is idempotent.
        Configs.register();
        registerAll();
    }

    private ModAttributesFabric() {}

    /**
     * No-op trampoline. Calling it forces this class's {@code <clinit>}, which
     * registers every mod attribute exactly once. Called from
     * {@code ModInitializer.onInitialize} (the primary path) and from
     * {@link #augment(AttributeSupplier)} (defensive — {@code getSupplier} is not
     * invoked before mod load completes, but the call is cheap).
     */
    public static void ensureInitialized() {}

    /**
     * Builds a copy of {@code original} that includes every Combat Attributes
     * holder in addition to the original's attributes. Existing entries keep
     * their base values. Called from {@code DefaultAttributesMixin}, which caches
     * the result so each entity type pays the rebuild cost once.
     *
     * <p>Reads {@code original.instances} via {@link AttributeSupplierAccessor}, since
     * vanilla's {@code AttributeSupplier.Builder} lacks a copy constructor (NeoForge has
     * one, but it is not in vanilla).
     */
    public static AttributeSupplier augment(AttributeSupplier original) {
        ensureInitialized();
        AttributeSupplier.Builder builder = AttributeSupplier.builder();
        Map<Holder<Attribute>, AttributeInstance> instances =
                ((AttributeSupplierAccessor) original).combat_attributes$getInstances();
        for (Map.Entry<Holder<Attribute>, AttributeInstance> entry : instances.entrySet()) {
            builder.add(entry.getKey(), entry.getValue().getBaseValue());
        }
        for (Holder<Attribute> holder : ModAttributes.allHolders()) {
            if (!original.hasAttribute(holder)) {
                builder.add(holder);
            }
        }
        return builder.build();
    }

    private static void registerAll() {
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
