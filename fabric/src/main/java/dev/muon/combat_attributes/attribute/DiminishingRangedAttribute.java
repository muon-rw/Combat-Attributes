package dev.muon.combat_attributes.attribute;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.function.Supplier;

/**
 * Fabric concrete attribute: extends vanilla {@link RangedAttribute}, implements
 * {@link DiminishingAttribute}. Bounds are read from the {@link AttributeSpec}
 * once at registration time (so changes need a restart). The diminishing formula
 * is read on every {@link #combine(double, AttributeModifier.Operation)} call,
 * so live config edits propagate.
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
