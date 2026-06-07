package dev.muon.combat_attributes.resource;

import dev.muon.combat_attributes.config.Configs;
import net.minecraft.world.entity.player.Player;

// Invoked from each loader's native pre-attack event to avoid a Player#attack mixin.
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
