package dev.muon.combat_attributes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.muon.combat_attributes.attribute.DiminishingAttribute;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Replaces vanilla per-operation linear summation with the attribute's diminishing formula
 * for any attribute that implements {@link DiminishingAttribute}. Vanilla attributes are
 * untouched (the {@code instanceof} check short-circuits).
 *
 * <p>Vanilla {@code calculateValue} (see source for {@link AttributeInstance}):
 * <pre>
 *   base   = baseValue + Σ ADD_VALUE.amount
 *   result = base + Σ ADD_MULTIPLIED_BASE.amount * base
 *   result *= Π (1 + ADD_MULTIPLIED_TOTAL.amount)
 *   return sanitizeValue(result)
 * </pre>
 *
 * <p>Replacement (when {@code instanceof DiminishingAttribute}):
 * <pre>
 *   addSum   = combine(Σ ADD_VALUE.amount,            ADD_VALUE)
 *   baseSum  = combine(Σ ADD_MULTIPLIED_BASE.amount,  ADD_MULTIPLIED_BASE)
 *   totalSum = combine(Σ ADD_MULTIPLIED_TOTAL.amount, ADD_MULTIPLIED_TOTAL)
 *   result   = (baseValue + addSum) * (1 + baseSum) * (1 + totalSum)
 *   return sanitizeValue(result)
 * </pre>
 *
 * <p>Vanilla applies ADD_MULTIPLIED_TOTAL multiplicatively per modifier (compounding); the
 * diminishing path collapses each operation's modifiers into a single combined factor.
 * That's intentional — the formula owns combination semantics.
 */
@Mixin(value = AttributeInstance.class, remap = false)
public class AttributeInstanceMixin {

    @ModifyReturnValue(method = "calculateValue", at = @At("RETURN"))
    private double combat_attributes$applyDiminishing(double original) {
        AttributeInstance self = (AttributeInstance) (Object) this;
        Attribute attr = self.getAttribute().value();
        if (!(attr instanceof DiminishingAttribute dim)) return original;

        double addRaw = 0.0, baseRaw = 0.0, totalRaw = 0.0;
        for (AttributeModifier m : self.getModifiers()) {
            switch (m.operation()) {
                case ADD_VALUE -> addRaw += m.amount();
                case ADD_MULTIPLIED_BASE -> baseRaw += m.amount();
                case ADD_MULTIPLIED_TOTAL -> totalRaw += m.amount();
            }
        }

        double base = self.getBaseValue();
        double result = (base + dim.combine(addRaw, Operation.ADD_VALUE))
                * (1.0 + dim.combine(baseRaw, Operation.ADD_MULTIPLIED_BASE))
                * (1.0 + dim.combine(totalRaw, Operation.ADD_MULTIPLIED_TOTAL));
        return attr.sanitizeValue(result);
    }
}
