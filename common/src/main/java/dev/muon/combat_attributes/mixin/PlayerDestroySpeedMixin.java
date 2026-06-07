package dev.muon.combat_attributes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.muon.combat_attributes.config.Configs;
import dev.muon.combat_attributes.resource.PlayerResources;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Scales destroy speed by {@code exhaustedBreakSpeedMultiplier} when stamina is zero.
 * Hooked at RETURN because getDestroySpeed runs on both sides during break-progress
 * prediction, so it must apply symmetrically.
 */
@Mixin(value = Player.class, remap = false)
public class PlayerDestroySpeedMixin {

    @ModifyReturnValue(method = "getDestroySpeed", at = @At("RETURN"))
    private float combat_attributes$exhaustedSlowdown(float original) {
        float multiplier = Configs.GENERAL.exhaustedBreakSpeedMultiplier.get().floatValue();
        if (multiplier == 1.0F) return original;
        Player self = (Player) (Object) this;
        if (PlayerResources.getStamina(self) > 0.0F) return original;
        return original * multiplier;
    }
}
