package dev.muon.combat_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.muon.combat_attributes.damage.DamageHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Drives Combat Attributes' damage pipeline by wrapping {@code LivingEntity#hurtServer}:
 * <ol>
 *   <li>Evasion roll — if the victim dodges, skip the original method entirely (returns
 *       {@code false}; vanilla treats this as no damage applied).</li>
 *   <li>Modify incoming damage — ranged flat bonus + crit multipliers per damage type.</li>
 *   <li>Call vanilla {@code hurtServer} with the modified amount.</li>
 *   <li>Lifesteal — if vanilla returned {@code true} (damage applied) AND the attacker is
 *       within their {@code entity_interaction_range} of the victim, heal the attacker.</li>
 * </ol>
 *
 * <p>{@code @WrapMethod} is the right tool here — we need to optionally skip the original
 * call (for evasion), modify args, and post-process the return value. {@code @ModifyVariable}
 * can't cancel and {@code @Inject} can't both modify the {@code damage} arg and conditionally
 * cancel from one handler.
 */
@Mixin(value = LivingEntity.class, remap = false)
public class LivingEntityHurtMixin {

    @WrapMethod(method = "hurtServer")
    private boolean combat_attributes$wrapHurt(ServerLevel level, DamageSource source, float damage,
                                                Operation<Boolean> original) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (DamageHandler.shouldDodge(self, source)) {
            return false;
        }

        float modified = DamageHandler.modifyIncomingDamage(self, source, damage);
        boolean applied = original.call(level, source, modified);

        if (applied) {
            DamageHandler.afterDamage(self, source, modified);
        }
        return applied;
    }
}
