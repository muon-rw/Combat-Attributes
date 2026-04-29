package dev.muon.combat_attributes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.muon.combat_attributes.config.Configs;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Suppresses vanilla's jump-crit damage bonus when {@code Configs.SYNC.disableVanillaJumpCrits}
 * is true. Targets {@code Player#canCriticalAttack} (the private predicate vanilla checks
 * before applying its hardcoded ×1.5 multiplier in {@code Player#attack}).
 */
@Mixin(value = Player.class, remap = false)
public class PlayerJumpCritMixin {

    @ModifyReturnValue(method = "canCriticalAttack", at = @At("RETURN"))
    private boolean combat_attributes$suppressJumpCrit(boolean original) {
        return original && !Configs.GENERAL.disableVanillaJumpCrits.get();
    }
}
