package dev.muon.combat_attributes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.muon.combat_attributes.attribute.DiminishingAttribute;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Routes any attribute that implements {@link DiminishingAttribute} through its
 * {@link DiminishingAttribute#combineAll combineAll} math instead of vanilla's
 * per-operation linear sum. Vanilla attributes (and Combat Attributes' LINEAR-mode
 * attrs, which don't implement {@code DiminishingAttribute}) are untouched — the
 * {@code instanceof} check short-circuits and the original return value is kept.
 *
 * <p>Vanilla {@code calculateValue}:
 * <pre>
 *   base   = baseValue + Σ ADD_VALUE.amount
 *   result = base + Σ ADD_MULTIPLIED_BASE.amount · base
 *   result *= Π (1 + ADD_MULTIPLIED_TOTAL.amount)
 *   return sanitizeValue(result)
 * </pre>
 *
 * <p>Replacement (when {@code instanceof DiminishingAttribute}):
 * <pre>
 *   result = dim.combineAll(baseValue, Σ ADD_VALUE, Σ ADD_MULTIPLIED_BASE, Σ ADD_MULTIPLIED_TOTAL)
 *   return sanitizeValue(result)
 * </pre>
 *
 * <p>The diminishing path collapses each operation's modifiers into a single sum before
 * combining, which differs from vanilla's per-modifier compounding for ADD_MULTIPLIED_TOTAL.
 * That's intentional — {@code AttributeSpec.combineAll} owns combination semantics, and
 * per-modifier compounding would let multiple "+50%" modifiers slip past any per-operation
 * soft cap.
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

        double result = dim.combineAll(self.getBaseValue(), addRaw, baseRaw, totalRaw);
        return attr.sanitizeValue(result);
    }
}
