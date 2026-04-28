package dev.muon.combat_attributes.attribute;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.common.PercentageAttribute;

import java.util.function.Supplier;

/**
 * NeoForge concrete attribute for percent-flavored entries that also need diminishing
 * stacking. Extends {@link PercentageAttribute} so NeoForge's tooltip pipeline renders
 * it as a percentage, and implements {@link DiminishingAttribute} so the common
 * {@code AttributeInstance#calculateValue} mixin routes its modifier sums through
 * the configured formulas.
 */
public class DiminishingPercentageAttribute extends PercentageAttribute implements DiminishingAttribute {

    private final Supplier<AttributeSpec> spec;

    public DiminishingPercentageAttribute(String descriptionId, Supplier<AttributeSpec> spec, double scaleFactor) {
        this(descriptionId, spec, spec.get(), scaleFactor);
    }

    private DiminishingPercentageAttribute(String descriptionId, Supplier<AttributeSpec> spec, AttributeSpec snapshot, double scaleFactor) {
        super(descriptionId, snapshot.defaultValue.get(), snapshot.minValue.get(), snapshot.maxValue.get(), scaleFactor);
        this.spec = spec;
    }

    @Override
    public double combine(double sum, AttributeModifier.Operation operation) {
        return spec.get().evaluate(sum, operation);
    }
}
