package dev.muon.combat_attributes.attribute;

import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedEnum;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;

/**
 * One attribute's full configuration: bounds and stacking math.
 *
 * <p>{@link #defaultValue} / {@link #minValue} / {@link #maxValue} are baked into the
 * underlying attribute at registration time, so changes there require a restart.
 * {@link #stackingMode} is also baked at startup — picking the concrete attribute class
 * (vanilla {@code RangedAttribute} for {@link StackingMode#LINEAR}, or a
 * {@link DiminishingAttribute} variant otherwise) — so that field also requires a restart.
 * {@link #softCap} and {@link #halfSaturation} are read on every value calculation, so
 * tuning those propagates live.
 *
 * <p>Stacking shape: each operation's modifier amounts are summed to {@code x}, then
 * transformed via {@code min(x, M*x/(x+k))} where {@code M = softCap} (per-operation
 * asymptote, the most a single operation can contribute) and {@code k = halfSaturation}
 * (controls how soon diminishing kicks in). The {@code min} guarantees diminishing only
 * ever <em>reduces</em> modifier potency, never amplifies — small modifiers pass through
 * linearly until the diminishing curve crosses below {@code x} at {@code x = M - k}, after
 * which it asymptotes toward {@code M}. So a +1% crit chance modifier on an attribute with
 * {@code M=0.4, k=0.05} contributes 1%, not the 6.7% the raw {@code M*x/(x+k)} curve would
 * give at {@code x = 0.01}. Per-operation contributions are then combined into a single value:
 * <ul>
 *   <li>{@link StackingMode#LINEAR}: vanilla math; {@link DiminishingAttribute} is not
 *       implemented and the mixin short-circuits. Use for unbounded scalars.</li>
 *   <li>{@link StackingMode#SOFT_CAP}: {@code base + add + mulBase + mulTotal}, each
 *       diminished. Each operation independently caps at {@code M}; total tops out near
 *       {@code base + 3M}. Use for damage multipliers and similar additive-bonus stats.</li>
 *   <li>{@link StackingMode#PROBABILISTIC}: {@code 1 - (1-base)(1-add)(1-mulBase)(1-mulTotal)},
 *       each diminished. Each operation contributes a probability up to {@code M};
 *       multiple sources combine via probabilistic union (two 40% sources → 64%, three
 *       → 78.4%, asymptote 100%). Use for chance-style attributes (crit, dodge, etc.).</li>
 *   <li>{@link StackingMode#MULTIPLICATIVE}: {@code base · (1-r_add)·(1-r_mulBase)·(1-r_mulTotal)}
 *       for the buff direction (negative modifier sums), with each {@code r_i} being the
 *       diminished reduction. Increases (positive sums) pass through linearly as factor
 *       {@code (1 + sum)}. The buff sign is flipped vs. the other diminishing modes —
 *       reductions are the soft-capped path here, since this mode targets multipliers
 *       like mana cost where lower is better. Per-slot cap is {@code M} (max reduction
 *       fraction); stacks via {@code 1 - Π(1-r_i)} on the reduction, equivalent to
 *       multiplicative stacking on the cost factor (two 30% sources → 51% reduction).</li>
 * </ul>
 *
 * <p>Diminishing applies to all three modifier operations the same way — there is no
 * "vanilla bypass" for {@code ADD_MULTIPLIED_BASE} or {@code ADD_MULTIPLIED_TOTAL}.
 * That is intentional: addons or items shouldn't be able to push a chance attribute past
 * the per-source cap by switching operation type. Within a non-LINEAR stacking mode,
 * the operation slot is a labelling convention only, used to spread sources across
 * three independent "buckets" that combine into the final value.
 */
public class AttributeSpec extends ConfigSection {

    @Comment("Base value applied to every living entity by default. Baked at startup; restart to apply changes.")
    public ValidatedDouble defaultValue;

    @Comment("Hard minimum the attribute clamps to. Baked at startup; restart to apply changes.")
    public ValidatedDouble minValue;

    @Comment("Hard maximum the attribute clamps to. Baked at startup; restart to apply changes.")
    public ValidatedDouble maxValue;

    @Comment("How modifiers stack. LINEAR = vanilla math (no soft cap). SOFT_CAP = additive with per-operation soft cap, " +
            "for damage multipliers. PROBABILISTIC = probabilistic union with per-operation soft cap, for chance attributes. " +
            "Baked at startup; restart to apply changes.")
    public ValidatedEnum<StackingMode> stackingMode;

    @Comment("Per-operation asymptote (M in M*x/(x+k)). The most a single operation slot can contribute. " +
            "For PROBABILISTIC chance attrs, set this to the desired per-source probability cap (e.g. 0.4 = 40%). " +
            "Inert when stackingMode = LINEAR. Read live; tune freely.")
    public ValidatedDouble softCap;

    @Comment("Diminishing shape parameter (k in M*x/(x+k)). Small modifiers pass through linearly; diminishing kicks " +
            "in at x = M - k and the curve asymptotes to M from there. Smaller k = diminishing kicks in earlier and " +
            "the asymptote is approached faster. Inert when stackingMode = LINEAR. Read live; tune freely.")
    public ValidatedDouble halfSaturation;

    public AttributeSpec() {
        // Required no-arg constructor for FzzyConfig deserialization.
        this(0.0, 0.0, 1.0, StackingMode.LINEAR, 0.0, 1.0);
    }

    public AttributeSpec(double defaultValue, double minValue, double maxValue,
                         StackingMode mode, double softCap, double halfSaturation) {
        // ValidatedDouble(default, max, min) — note FzzyConfig's argument order.
        this.defaultValue = new ValidatedDouble(defaultValue, 1_000_000.0, -1_000_000.0);
        this.minValue = new ValidatedDouble(minValue, 1_000_000.0, -1_000_000.0);
        this.maxValue = new ValidatedDouble(maxValue, 1_000_000.0, -1_000_000.0);
        this.stackingMode = new ValidatedEnum<>(mode);
        this.softCap = new ValidatedDouble(softCap, 1_000_000.0, -1_000_000.0);
        // halfSaturation must stay positive to avoid divide-by-zero on the first modifier.
        this.halfSaturation = new ValidatedDouble(halfSaturation, 1_000_000.0, 1.0e-6);
    }

    /**
     * Combines base value + per-operation modifier sums into the final attribute value.
     * Caller (the mixin) collects raw sums per operation and hands them in here; this
     * method owns all the combination math.
     */
    public double combineAll(double base, double addRaw, double mulBaseRaw, double mulTotalRaw) {
        StackingMode mode = stackingMode.get();
        if (mode == StackingMode.LINEAR) {
            // Should not normally be hit (LINEAR attrs don't implement DiminishingAttribute and
            // therefore skip the mixin path), but kept here as a sane fallback.
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

    /** Inverse-sign analog of {@link #diminishOp} for {@link StackingMode#MULTIPLICATIVE}: reductions diminish, increases pass through linearly. */
    private double multiplicativeFactor(double sum) {
        if (sum >= 0.0) return 1.0 + sum;
        return 1.0 - softCapped(-sum);
    }

    private double softCapped(double r) {
        // min() guarantees diminishing never amplifies — the linear ramp x dominates until it
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
     * in {@link dev.muon.combat_attributes.config.ConfigAttributes}. See the class-level
     * doc on {@link AttributeSpec} for the math behind each mode.
     */
    public enum StackingMode {
        LINEAR,
        SOFT_CAP,
        PROBABILISTIC,
        MULTIPLICATIVE
    }
}
