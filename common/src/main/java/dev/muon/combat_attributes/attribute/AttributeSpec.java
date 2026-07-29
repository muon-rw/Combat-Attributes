package dev.muon.combat_attributes.attribute;

import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedEnum;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;

/**
 * One attribute's full configuration: bounds and stacking math.
 *
 * <p>{@link #defaultValue}, {@link #minValue}, {@link #maxValue} and {@link #stackingMode}
 * are baked into the underlying attribute at registration, so they need a restart;
 * {@link #softCap} and {@link #halfSaturation} are read per value calculation and tune live.
 * The {@code min()} in {@link #softCapped} guarantees diminishing never amplifies a modifier,
 * only reduces it. See {@link #combineAll} for the per-mode combination math.
 *
 * <p>Diminishing applies to all three modifier operations the same way; there is no
 * "vanilla bypass" for {@code ADD_MULTIPLIED_BASE} or {@code ADD_MULTIPLIED_TOTAL}.
 * That is intentional: addons or items shouldn't be able to push a chance attribute past
 * the per-source cap by switching operation type. Within a non-LINEAR stacking mode,
 * the operation slot is a labelling convention only, used to spread sources across
 * three independent "buckets" that combine into the final value.
 */
public class AttributeSpec extends ConfigSection {

    @Comment("Base value given to every living entity. Restart to apply.")
    public ValidatedDouble defaultValue;

    @Comment("Hard minimum the value clamps to. Restart to apply.")
    public ValidatedDouble minValue;

    @Comment("Hard maximum the value clamps to. Restart to apply.")
    public ValidatedDouble maxValue;

    @Comment("How modifiers stack. LINEAR = vanilla, no cap. SOFT_CAP = diminishing returns, for damage-style stats. " +
            "PROBABILISTIC = diminishing returns, for chance stats (crit, dodge). Restart to apply.")
    public ValidatedEnum<StackingMode> stackingMode;

    @Comment("Soft cap on how much a single source can add. For PROBABILISTIC, this is the per-source chance cap " +
            "(0.4 = 40%). Ignored when LINEAR. Applies live.")
    public ValidatedDouble softCap;

    @Comment("Controls how fast diminishing returns kick in. Smaller = kicks in sooner. " +
            "Ignored when LINEAR. Applies live.")
    public ValidatedDouble halfSaturation;

    public AttributeSpec() {
        // Required no-arg constructor for FzzyConfig deserialization.
        this(0.0, 0.0, 1.0, StackingMode.LINEAR, 0.0, 1.0);
    }

    public AttributeSpec(double defaultValue, double minValue, double maxValue,
                         StackingMode mode, double softCap, double halfSaturation) {
        // ValidatedDouble(default, max, min): FzzyConfig's argument order.
        this.defaultValue = new ValidatedDouble(defaultValue, 1_000_000.0, -1_000_000.0);
        this.minValue = new ValidatedDouble(minValue, 1_000_000.0, -1_000_000.0);
        this.maxValue = new ValidatedDouble(maxValue, 1_000_000.0, -1_000_000.0);
        this.stackingMode = new ValidatedEnum<>(mode);
        this.softCap = new ValidatedDouble(softCap, 1_000_000.0, -1_000_000.0);
        // halfSaturation must stay positive to avoid divide-by-zero on the first modifier.
        this.halfSaturation = new ValidatedDouble(halfSaturation, 1_000_000.0, 1.0e-6);
    }

    /**
     * Caller (the mixin) collects raw sums per operation and hands them in here; this
     * method owns all the combination math. Each operation's sum is diminished via
     * {@link #softCapped} (asymptote {@code M = softCap}, knee {@code k = halfSaturation}),
     * then combined per mode:
     * <ul>
     *   <li>{@link StackingMode#LINEAR}: vanilla math; short-circuited, kept as fallback.</li>
     *   <li>{@link StackingMode#SOFT_CAP}: {@code base + add + mulBase + mulTotal}, each
     *       caps at {@code M}, total tops out near {@code base + 3M}.</li>
     *   <li>{@link StackingMode#PROBABILISTIC}: {@code 1 - (1-base)(1-add)(1-mulBase)(1-mulTotal)};
     *       sources combine via probabilistic union (two 40% sources -> 64%, three -> 78.4%).</li>
     *   <li>{@link StackingMode#MULTIPLICATIVE}: see {@link #multiplicativeFactor}.</li>
     * </ul>
     */
    public double combineAll(double base, double addRaw, double mulBaseRaw, double mulTotalRaw) {
        StackingMode mode = stackingMode.get();
        if (mode == StackingMode.LINEAR) {
            // Shouldn't normally be hit (LINEAR attrs don't implement DiminishingAttribute and
            // skip the mixin path), but kept as a sane fallback.
            return base + addRaw + mulBaseRaw * base + mulTotalRaw * (base + addRaw);
        }
        if (mode == StackingMode.MULTIPLICATIVE) {
            return base
                    * multiplicativeFactor(addRaw)
                    * multiplicativeFactor(mulBaseRaw)
                    * multiplicativeFactor(mulTotalRaw);
        }
        double a = diminishOp(addRaw);
        double b = diminishOp(mulBaseRaw);
        double c = diminishOp(mulTotalRaw);
        return switch (mode) {
            case SOFT_CAP -> base + a + b + c;
            case PROBABILISTIC -> {
                double bp = clamp01(base);
                double ap = clamp01(a);
                double bbp = clamp01(b);
                double cp = clamp01(c);
                yield 1.0 - (1.0 - bp) * (1.0 - ap) * (1.0 - bbp) * (1.0 - cp);
            }
            // LINEAR and MULTIPLICATIVE are short-circuited above; reaching them here means a new mode was added without a case.
            case LINEAR, MULTIPLICATIVE -> throw new IllegalStateException("Unreachable: " + mode);
        };
    }

    private double diminishOp(double sum) {
        // Negative sums (debuffs) bypass the soft cap and pass through linearly so they actually subtract.
        // Otherwise M*x/(x+k) would soften them and, near x = -k, blow up.
        if (sum <= 0.0) return sum;
        return softCapped(sum);
    }

    /**
     * Inverse-sign analog of {@link #diminishOp} for {@link StackingMode#MULTIPLICATIVE}:
     * reductions are the soft-capped path (this mode targets multipliers like mana cost,
     * where lower is better), increases pass through linearly as {@code 1 + sum}. Per-slot
     * cap is the max reduction fraction {@code M}; slots stack via {@code 1 - product(1-r_i)}
     * on the reduction, equivalent to multiplicative stacking on the cost factor
     * (two 30% sources -> 51% reduction).
     */
    private double multiplicativeFactor(double sum) {
        if (sum >= 0.0) return 1.0 + sum;
        return 1.0 - softCapped(-sum);
    }

    private double softCapped(double r) {
        // min() guarantees diminishing never amplifies: the linear ramp x dominates until it
        // crosses M*x/(x+k) at x = M - k, after which the diminishing curve takes over.
        double m = softCap.get();
        double k = halfSaturation.get();
        return Math.min(r, m * r / (r + k));
    }

    private static double clamp01(double v) {
        if (v < 0.0) return 0.0;
        if (v > 1.0) return 1.0;
        return v;
    }

    /**
     * How an attribute's modifier sums combine into its final value. Decided per-attribute
     * in {@link dev.muon.combat_attributes.config.ConfigAttributes}. See {@link #combineAll}
     * for the math behind each mode.
     */
    public enum StackingMode {
        LINEAR,
        SOFT_CAP,
        PROBABILISTIC,
        MULTIPLICATIVE
    }
}
