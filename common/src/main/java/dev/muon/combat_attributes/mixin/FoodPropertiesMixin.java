package dev.muon.combat_attributes.mixin;

import dev.muon.combat_attributes.feature.LegacyHunger;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Reroutes the food/saturation grant in {@link FoodProperties#onConsume} into a direct heal when
 * the legacy-hunger feature is on. {@link Consumable#onConsume} still runs all other consume
 * effects (potions, sounds, particles, cooldowns) since it iterates effects independently of this
 * call — only the hunger-bar refill is reinterpreted.
 */
@Mixin(value = FoodProperties.class, remap = false)
public abstract class FoodPropertiesMixin {

    @Inject(method = "onConsume", at = @At("HEAD"), cancellable = true)
    private void combat_attributes$redirectToHeal(Level level, LivingEntity user, ItemStack stack,
                                                  Consumable consumable, CallbackInfo ci) {
        FoodProperties self = (FoodProperties) (Object) this;
        if (LegacyHunger.applyOnConsume(user, self)) {
            ci.cancel();
        }
    }
}
