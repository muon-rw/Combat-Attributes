package dev.muon.combat_attributes.attribute;

import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedExpression;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Map;
import java.util.Set;

/**
 * One attribute's full configuration: bounds + per-operation diminishing formulas.
 *
 * <p>{@link #defaultValue} / {@link #minValue} / {@link #maxValue} are baked into the
 * underlying {@code RangedAttribute} at registration time, so changes to those fields
 * require a restart to take effect. The formula fields are read on every
 * {@code AttributeInstance#getValue()} call (cached behind FzzyConfig's getter), so
 * formula edits propagate live.
 *
 * <p>Formula variable {@code x} is the sum of {@code modifier.amount()} for every
 * modifier of the given operation on the entity. Use {@code "x"} for linear /
 * non-diminishing behaviour.
 */
public class AttributeSpec extends ConfigSection {

    @Comment("Base value applied to every living entity by default. Baked at startup; restart to apply changes.")
    public ValidatedDouble defaultValue;

    @Comment("Hard minimum the attribute clamps to. Baked at startup; restart to apply changes.")
    public ValidatedDouble minValue;

    @Comment("Hard maximum the attribute clamps to. Baked at startup; restart to apply changes.")
    public ValidatedDouble maxValue;

    @Comment("Formula combining ADD_VALUE modifiers. 'x' = raw sum of amounts. Use 'x' for vanilla linear stacking.")
    public ValidatedExpression addValueFormula;

    @Comment("Formula combining ADD_MULTIPLIED_BASE modifiers. 'x' = raw sum of amounts.")
    public ValidatedExpression addMultipliedBaseFormula;

    @Comment("Formula combining ADD_MULTIPLIED_TOTAL modifiers. 'x' = raw sum of amounts.")
    public ValidatedExpression addMultipliedTotalFormula;

    public AttributeSpec() {
        // Required no-arg constructor for FzzyConfig deserialization.
        this(0.0, 0.0, 1.0, "x");
    }

    public AttributeSpec(double defaultValue, double minValue, double maxValue, String formula) {
        // ValidatedDouble(default, max, min) — note FzzyConfig's argument order.
        this.defaultValue = new ValidatedDouble(defaultValue, 1_000_000.0, -1_000_000.0);
        this.minValue = new ValidatedDouble(minValue, 1_000_000.0, -1_000_000.0);
        this.maxValue = new ValidatedDouble(maxValue, 1_000_000.0, -1_000_000.0);
        this.addValueFormula = new ValidatedExpression(formula, Set.of('x'));
        this.addMultipliedBaseFormula = new ValidatedExpression(formula, Set.of('x'));
        this.addMultipliedTotalFormula = new ValidatedExpression(formula, Set.of('x'));
    }

    /** Evaluates the per-operation formula. Falls back to the raw sum on parse / eval failure. */
    public double evaluate(double sum, AttributeModifier.Operation op) {
        Map<Character, Double> vars = Map.of('x', sum);
        return switch (op) {
            case ADD_VALUE -> addValueFormula.evalSafe(vars, sum);
            case ADD_MULTIPLIED_BASE -> addMultipliedBaseFormula.evalSafe(vars, sum);
            case ADD_MULTIPLIED_TOTAL -> addMultipliedTotalFormula.evalSafe(vars, sum);
        };
    }
}
