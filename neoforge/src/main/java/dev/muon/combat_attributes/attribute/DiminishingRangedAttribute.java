package dev.muon.combat_attributes.attribute;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.function.Supplier;

/**
 * NeoForge concrete attribute. Currently extends vanilla {@link RangedAttribute};
 * once Dynamic Tooltips owns percent-display config (Fabric) we can swap the
 * superclass to NeoForge's {@code PercentageAttribute} on a per-attribute basis
 * for the percent-flavored ones — same {@link DiminishingAttribute} contract either way.
 */
public class DiminishingRangedAttribute extends RangedAttribute implements DiminishingAttribute {

    private final Supplier<AttributeSpec> spec;

    public DiminishingRangedAttribute(String descriptionId, Supplier<AttributeSpec> spec) {
        this(descriptionId, spec, spec.get());
    }

    private DiminishingRangedAttribute(String descriptionId, Supplier<AttributeSpec> spec, AttributeSpec snapshot) {
        super(descriptionId, snapshot.defaultValue.get(), snapshot.minValue.get(), snapshot.maxValue.get());
        this.spec = spec;
    }

    @Override
    public double combine(double sum, AttributeModifier.Operation operation) {
        return spec.get().evaluate(sum, operation);
    }
}
