package dev.muon.combat_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.muon.combat_attributes.attribute.ModAttributes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Implements {@code experience_gain} by scaling only the XP that ends up in the player's
 * XP bar. Wraps the {@code Player#giveExperiencePoints} call inside
 * {@code ExperienceOrb#playerTouch}, so the orb's stored value, mending repair, and any
 * non-orb XP source ({@code /xp} command, advancement rewards, custom code) are all
 * untouched. Only the bar-bound integer is multiplied.
 *
 * Intentional that this excludes other sources for now, until we can consider
 * which cases "other sources" actually entails in a modded context
 */
@Mixin(value = ExperienceOrb.class, remap = false)
public class ExperienceOrbMixin {

    @WrapOperation(method = "playerTouch",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;giveExperiencePoints(I)V"))
    private void combat_attributes$scaleOrbXp(Player player, int amount, Operation<Void> original) {
        double mult = ModAttributes.valueOrDefault(player, ModAttributes.experienceGain());
        if (mult == 1.0) {
            original.call(player, amount);
            return;
        }
        long scaled = Math.round((double) amount * mult);
        int adjusted = (int) Math.max(0L, Math.min((long) Integer.MAX_VALUE, scaled));
        original.call(player, adjusted);
    }
}
