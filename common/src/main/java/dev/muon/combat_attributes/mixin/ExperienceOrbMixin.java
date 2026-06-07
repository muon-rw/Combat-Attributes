package dev.muon.combat_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.muon.combat_attributes.attribute.ModAttributes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ExperienceOrb.class, remap = false)
public class ExperienceOrbMixin {

    // Scales only the bar-bound XP; orb-stored value, mending repair, and non-orb sources are intentionally untouched.
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
