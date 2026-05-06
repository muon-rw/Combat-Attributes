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
 * in the post-exhaustion lockout, gated by {@code attackStaminaCost > 0}). On
 * full-cooldown swings ({@code attackStrengthScale ≥ 0.9}), two side effects
 * fire on the server: the configured cost is drained (paying for the muscle
 * effort regardless of whether the swing landed, so whiffs and shield-blocks
 * still cost), and the regen-delay timer is armed from
 * {@code attackPauseStaminaRegenSeconds}. The two are independently gated —
 * either or both can be disabled by zeroing their respective configs.
 */
public final class AttackStaminaHandler {

    private AttackStaminaHandler() {}

    public static boolean shouldCancelAttack(Player player) {
        float cost = Configs.GENERAL.attackStaminaCost.get().floatValue();
        double pauseSeconds = Configs.GENERAL.attackPauseStaminaRegenSeconds.get();

        if (cost > 0.0F && !PlayerResources.canSpendStamina(player)) return true;
        if (player.level().isClientSide()) return false;
        if (player.getAttackStrengthScale(0.5F) < 0.9F) return false;

        if (cost > 0.0F) {
            PlayerResources.trySpendStamina(player, cost);
        }
        if (pauseSeconds > 0.0) {
            PlayerResources.armStaminaRegenDelay(player, (int) Math.round(pauseSeconds * 20.0));
        }
        return false;
    }
}
