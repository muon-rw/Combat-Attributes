package dev.muon.combat_attributes.attribute;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Marker interface for attributes whose modifiers combine via a soft-cap formula
 * rather than vanilla's pure linear sum / multiplicative product per operation.
 *
 * <p>Concrete classes typically extend either vanilla {@code RangedAttribute} (Fabric)
 * or NeoForge's {@code PercentageAttribute} (NeoForge) and implement this. The common
 * {@code AttributeInstance#calculateValue} mixin checks {@code instanceof DiminishingAttribute}
 * and replaces vanilla per-operation summation with {@link #combine(double, AttributeModifier.Operation)}.
 *
 * <p>Diminishing applies to all three operations:
 * {@code ADD_VALUE}, {@code ADD_MULTIPLIED_BASE}, {@code ADD_MULTIPLIED_TOTAL}. An implementation
 * that wants linear (non-diminished) behaviour for some operation should return {@code sum}
 * unchanged for that case.
 */
public interface DiminishingAttribute {

    /**
     * Combines the raw sum of modifier amounts for a single operation into the value
     * that vanilla would otherwise have summed linearly. The mixin then applies it as:
     * <pre>
     *   result = (base + combine(addValueSum, ADD_VALUE))
     *          * (1 + combine(addBaseSum,  ADD_MULTIPLIED_BASE))
     *          * (1 + combine(addTotalSum, ADD_MULTIPLIED_TOTAL))
     * </pre>
     *
     * @param sum sum of {@code modifier.amount()} for every modifier of this {@code operation}
     * @param operation which operation this sum was collected for
     * @return diminished value to substitute for the raw sum
     */
    double combine(double sum, AttributeModifier.Operation operation);
}
