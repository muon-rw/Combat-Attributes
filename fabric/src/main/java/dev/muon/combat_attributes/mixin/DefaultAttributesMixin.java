package dev.muon.combat_attributes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.muon.combat_attributes.attribute.ModAttributesFabric;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Why not target {@code LivingEntity#createLivingAttributes}: that helper is a
 * convention, not a contract. Mod entities that build their supplier from scratch via
 * {@code AttributeSupplier.builder()} skip the helper entirely, and our attributes would
 * never reach them. {@code DefaultAttributes#getSupplier} is the single lookup point every
 * supplier consumer goes through, so wrapping here catches every entity type uniformly.
 */
@Mixin(value = DefaultAttributes.class, remap = false)
public class DefaultAttributesMixin {

    @Unique
    private static final Map<EntityType<?>, AttributeSupplier> combat_attributes$cache = new ConcurrentHashMap<>();

    @ModifyReturnValue(method = "getSupplier", at = @At("RETURN"))
    private static AttributeSupplier combat_attributes$augment(
            AttributeSupplier original,
            @Local(argsOnly = true) EntityType<?> entityType) {
        if (original == null) return null;
        return combat_attributes$cache.computeIfAbsent(entityType,
                type -> ModAttributesFabric.augment(original, type));
    }
}
