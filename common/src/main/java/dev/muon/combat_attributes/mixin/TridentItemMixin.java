package dev.muon.combat_attributes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.muon.combat_attributes.config.Configs;
import dev.muon.combat_attributes.resource.PlayerResources;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Drains {@code tridentThrowStaminaCost} when a trident is actually thrown.
 *
 * <p>{@code TridentItem#releaseUsing} returns {@code true} only on a successful
 * throw; tap-releases (timeHeld &lt; 10), unbreakable-trident edge cases, and
 * failed riptide-condition checks all return {@code false} and skip the drain.
 * Reading the boolean return via {@link ModifyReturnValue} is the cleanest way
 * to gate on "vanilla decided to fire," matching the user-facing semantic of
 * "you only pay for throws that actually happen."
 *
 * <p>Tridents are not covered by {@link LivingEntityMixin}'s ranged-draw drain
 * (which keys off {@link net.minecraft.world.item.ProjectileWeaponItem}, a class
 * trident does not extend), and the per-tick draw model wouldn't fit them anyway:
 * a trident is held to charge but the throw itself is the meaningful event.
 */
@Mixin(value = TridentItem.class, remap = false)
public abstract class TridentItemMixin {

    @ModifyReturnValue(method = "releaseUsing", at = @At("RETURN"))
    private boolean combat_attributes$drainStaminaOnThrow(boolean threw,
                                                          ItemStack stack, Level level,
                                                          LivingEntity entity, int remainingTime) {
        if (!threw) return threw;
        if (!(entity instanceof Player player)) return threw;
        if (player.level().isClientSide()) return threw;

        PlayerResources.trySpendStamina(player, Configs.GENERAL.tridentThrowStaminaCost.get().floatValue());
        return threw;
    }
}
