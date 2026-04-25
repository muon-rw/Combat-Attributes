package dev.muon.combat_attributes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.muon.combat_attributes.attribute.ModAttributes;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Attaches every Combat Attributes attribute to every living entity via
 * {@code LivingEntity#createLivingAttributes}. NeoForge handles this via
 * {@code EntityAttributeModificationEvent} on its side.
 */
@Mixin(value = LivingEntity.class, remap = false)
public class LivingEntityMixin {

    @ModifyReturnValue(method = "createLivingAttributes", at = @At("RETURN"))
    private static AttributeSupplier.Builder combat_attributes$addAttributes(AttributeSupplier.Builder original) {
        for (Holder<Attribute> holder : ModAttributes.allHolders()) {
            original.add(holder);
        }
        return original;
    }
}
