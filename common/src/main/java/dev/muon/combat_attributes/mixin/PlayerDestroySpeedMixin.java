package dev.muon.combat_attributes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.muon.combat_attributes.config.Configs;
import dev.muon.combat_attributes.resource.PlayerResources;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Mining-fatigue-style slowdown for exhausted players. When stamina is at zero,
 * scales the destroy-speed return by {@code exhaustedBreakSpeedMultiplier}.
 *
 * <p>Vanilla's own {@code MINING_FATIGUE} effect applies a similar multiplier
 * inside {@code Player#getDestroySpeed} (0.3 / 0.09 / 0.0027 / 8.1E-4 by amplifier).
 * Hooking with {@link ModifyReturnValue} at {@code RETURN} lets us layer on top of
 * any in-method scaling — vanilla mining fatigue stacks multiplicatively. Reading
 * stamina via {@link PlayerResources} works on both client and server because the
 * resource attachment is auto-synced; {@code getDestroySpeed} is called on both
 * sides during break progress prediction so this needs to apply symmetrically.
 *
 * <p>A multiplier of exactly {@code 1.0} short-circuits the ModifyReturnValue write
 * so the disabled-feature path produces no measurable overhead.
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
