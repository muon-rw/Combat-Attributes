package dev.muon.combat_attributes.mixin;

import dev.muon.combat_attributes.feature.HealthRegenTicker;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric-only per-entity {@code health_regeneration} hook. Fabric has no per-entity
 * tick event, so regen is driven straight off {@link LivingEntity#tick()} rather than
 * sweeping every loaded entity each server tick. {@code TAIL} matches the end-of-tick
 * timing of the NeoForge {@code EntityTickEvent.Post} handler; the server-side guard
 * mirrors it too, since {@code tick} runs on both logical sides.
 */
@Mixin(value = LivingEntity.class, remap = false)
public abstract class LivingEntityHealthRegenMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void combat_attributes$healthRegenTick(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()) return;
        HealthRegenTicker.onLivingTick(self);
    }
}
