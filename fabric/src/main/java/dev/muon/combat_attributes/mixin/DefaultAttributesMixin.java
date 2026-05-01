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
 * Augments every {@link AttributeSupplier} returned by {@link DefaultAttributes#getSupplier}
 * to include this mod's combat attributes, so they appear on every living entity regardless
 * of how the entity's supplier was originally built.
 *
 * <p>This mirrors NeoForge's {@code CommonHooks#modifyAttributes} pattern. The original
 * supplier's {@code instances} field is read via {@link AttributeSupplierAccessor}; the
 * augmented supplier preserves every original attribute (with its original base value) and
 * adds our holders. Results are cached per entity type so each type only pays the rebuild
 * cost once.
 *
 * <p>Why not target {@code LivingEntity#createLivingAttributes}: that helper is a
 * convention, not a contract. Mod entities that build their supplier from scratch via
 * {@code AttributeSupplier.builder()} skip the helper entirely, and our attributes would
 * never reach them. {@code DefaultAttributes#getSupplier} is the single lookup point every
 * supplier consumer goes through, so wrapping here catches every entity type uniformly.
 *
 * <p>The {@link EntityType} is captured via {@link Local @Local(argsOnly = true)} so the
 * augment step can skip {@code playerOnly} attributes for non-player suppliers.
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
