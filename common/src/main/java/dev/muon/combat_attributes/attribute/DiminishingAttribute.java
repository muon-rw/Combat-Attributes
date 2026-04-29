package dev.muon.combat_attributes.attribute;

/**
 * Marker interface for attributes whose modifiers combine via {@link AttributeSpec#combineAll}
 * (soft cap / probabilistic union) rather than vanilla's per-operation linear sum.
 *
 * <p>Concrete classes typically extend either vanilla {@code RangedAttribute} (Fabric)
 * or NeoForge's {@code PercentageAttribute} (NeoForge) and implement this. The common
 * {@code AttributeInstance#calculateValue} mixin checks {@code instanceof DiminishingAttribute}
 * and delegates the entire base + ops → final-value math to {@link #combineAll}; vanilla
 * attributes keep their original behavior unchanged.
 *
 * <p>Linear-stacking attributes ({@link AttributeSpec.StackingMode#LINEAR}) don't implement
 * this interface, so the mixin short-circuits and they pay zero cost relative to vanilla.
 */
public interface DiminishingAttribute {

    /**
     * Computes the final attribute value from the base value and the raw per-operation
     * modifier sums. Replaces vanilla's
     * {@code (base + Σadd) + ΣmulBase·base + (Π(1+mulTotal) − 1)·…} entirely.
     *
     * @param base         the entity's base value for this attribute
     * @param addRaw       sum of every {@code ADD_VALUE} modifier amount
     * @param mulBaseRaw   sum of every {@code ADD_MULTIPLIED_BASE} modifier amount
     * @param mulTotalRaw  sum of every {@code ADD_MULTIPLIED_TOTAL} modifier amount
     * @return final value before {@code sanitizeValue} clamping
     */
    double combineAll(double base, double addRaw, double mulBaseRaw, double mulTotalRaw);

    /**
     * Per-operation asymptote — the most any single operation slot can contribute,
     * a.k.a. the "M" in {@code M*x/(x+k)}. For PROBABILISTIC stacking this is the
     * per-source probability cap; for SOFT_CAP it's the maximum additive bonus.
     * Exposed for UI use (tooltips that show players the soft-cap value).
     */
    double softCap();

    /**
     * {@code true} if this attribute uses probabilistic-union stacking (multiple sources
     * combine via {@code 1 - Π(1-p)}); {@code false} if it uses additive soft-cap stacking
     * (sources sum). Used by UI code to phrase tooltips appropriately.
     */
    boolean isProbabilistic();
}
