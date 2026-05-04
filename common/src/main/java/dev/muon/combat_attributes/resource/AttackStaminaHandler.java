package dev.muon.combat_attributes.resource;

import dev.muon.combat_attributes.config.Configs;
import net.minecraft.world.entity.player.Player;

/**
 * Shared decision logic for the attack-stamina gate, called from each loader's
 * native pre-attack event ({@code AttackEntityEvent} on NeoForge,
 * {@code AttackEntityCallback} on Fabric) — eliminates the need for a common
 * mixin on {@code Player#attack}.
 *
 * <p>Returns {@code true} to indicate the swing should be cancelled (player is
 * in the post-exhaustion lockout). On full-cooldown swings ({@code attackStrengthScale ≥ 0.9}),
 * the configured cost is also drained as a side effect — paying for the muscle
 * effort regardless of whether the swing landed, so whiffs and shield-blocks
 * still cost.
 */
public final class AttackStaminaHandler {

    private AttackStaminaHandler() {}

    public static boolean shouldCancelAttack(Player player) {
        float cost = Configs.GENERAL.attackStaminaCost.get().floatValue();
        if (cost <= 0.0F) return false;
        if (!PlayerResources.canSpendStamina(player)) return true;
        if (player.level().isClientSide()) return false;
        if (player.getAttackStrengthScale(0.5F) >= 0.9F) {
            PlayerResources.trySpendStamina(player, cost);
        }
        return false;
    }
}
