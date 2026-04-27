package dev.muon.combat_attributes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.muon.combat_attributes.attribute.ModAttributes;
import dev.muon.combat_attributes.attribute.ModAttributesFabric;
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
 *
 * <p>The {@link ModAttributesFabric#ensureInitialized()} call exists to force
 * registration if {@code DefaultAttributes.<clinit>} happens to fire before our
 * {@code onInitialize} entrypoint runs (Fabric does not order these). The static
 * initializer there is idempotent, so calling from both this mixin and
 * {@code onInitialize} is fine.
 */
@Mixin(value = LivingEntity.class, remap = false)
public class LivingEntityMixin {

    @ModifyReturnValue(method = "createLivingAttributes", at = @At("RETURN"))
    private static AttributeSupplier.Builder combat_attributes$addAttributes(AttributeSupplier.Builder original) {
        ModAttributesFabric.ensureInitialized();
        for (Holder<Attribute> holder : ModAttributes.allHolders()) {
            original.add(holder);
        }
        return original;
    }
}
