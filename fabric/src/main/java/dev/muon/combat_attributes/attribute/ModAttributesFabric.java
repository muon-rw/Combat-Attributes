package dev.muon.combat_attributes.attribute;

import dev.muon.combat_attributes.CombatAttributes;
import dev.muon.combat_attributes.config.Configs;
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
 * <p>Registration runs from the class's static initializer. Both the
 * {@code LivingEntity#createLivingAttributes} mixin and the
 * {@code ModInitializer.onInitialize} entrypoint reach registration through
 * {@link #ensureInitialized()}, which is a no-op call that exists only to force
 * {@code <clinit>}. Whichever runs first wins; subsequent calls are no-ops because
 * the JVM runs {@code <clinit>} exactly once.
 *
 * <p>Why eager class-init instead of registering on {@code onInitialize}: nothing in
 * Fabric guarantees that {@code DefaultAttributes.<clinit>} (which builds every
 * vanilla entity's {@link net.minecraft.world.entity.ai.attributes.AttributeSupplier})
 * runs after our entrypoint. If it raced ahead, the {@code createLivingAttributes}
 * mixin would fire against an empty holder map and entities would ship without our
 * attributes — matching the symptom this class was rewritten to fix.
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
     * registers every mod attribute exactly once. Both the Fabric mixin and the
     * mod entrypoint call this so registration is guaranteed before the first read,
     * regardless of which path the JVM hits first.
     */
    public static void ensureInitialized() {}

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
