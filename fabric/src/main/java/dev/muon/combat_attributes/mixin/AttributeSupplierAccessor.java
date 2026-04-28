package dev.muon.combat_attributes.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Exposes {@code AttributeSupplier#instances} so {@code ModAttributesFabric.augment} can
 * iterate the original entries when building a Combat Attributes-augmented copy. Replaces
 * the previous {@code combat_attributes.accesswidener} entry — keeps the Fabric-only
 * vanilla peek confined to the Fabric module instead of bleeding through {@code common}.
 *
 * <p>NeoForge has its own copy constructor on {@code AttributeSupplier.Builder}, so this
 * accessor is intentionally Fabric-only.
 */
@Mixin(value = AttributeSupplier.class, remap = false)
public interface AttributeSupplierAccessor {

    @Accessor("instances")
    Map<Holder<Attribute>, AttributeInstance> combat_attributes$getInstances();
}
