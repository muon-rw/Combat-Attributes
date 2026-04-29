package dev.muon.combat_attributes.attribute;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.function.Supplier;

/**
 * Fabric concrete attribute: extends vanilla {@link RangedAttribute}, implements
 * {@link DiminishingAttribute}. Bounds are read from the {@link AttributeSpec}
 * once at registration time (so changes need a restart). Soft-cap parameters are
 * read on every {@link #combineAll} call, so live config edits propagate.
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
    public double combineAll(double base, double addRaw, double mulBaseRaw, double mulTotalRaw) {
        return spec.get().combineAll(base, addRaw, mulBaseRaw, mulTotalRaw);
    }

    @Override
    public double softCap() {
        return spec.get().softCap.get();
    }

    @Override
    public boolean isProbabilistic() {
        return spec.get().stackingMode.get() == AttributeSpec.StackingMode.PROBABILISTIC;
    }
}
