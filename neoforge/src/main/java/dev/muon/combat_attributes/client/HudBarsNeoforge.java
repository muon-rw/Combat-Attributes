package dev.muon.combat_attributes.client;

import dev.muon.combat_attributes.CombatAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * NeoForge HUD wiring for the stamina + mana bars.
 *
 * <p>Each layer renders relative to {@code Gui.rightHeight} — NeoForge resets
 * that field to 39 every frame and bumps it by 10 after every right-side bar
 * draws (food, vehicle health, …) so the air bar above can read it as its
 * top-of-stack offset. Our layers participate by reading {@code rightHeight}
 * for their own Y, then bumping it by 10 only when actually drawn. That single
 * gate is what makes the air bubble layer shift up only while the stamina or
 * mana bar is visible — no mixin required, NeoForge's existing layout is the
 * right hook.
 *
 * <p>Layers register via {@link RegisterGuiLayersEvent#registerAbove}: stamina
 * goes above {@code FOOD_LEVEL} (so its layer-call follows food, by which
 * point {@code rightHeight} has been bumped to 49) and mana goes above stamina
 * (rightHeight 59). The {@code AIR_LEVEL} layer registered after both then
 * reads {@code rightHeight} to position itself, lifting up automatically.
 */
@EventBusSubscriber(modid = CombatAttributes.MOD_ID, value = Dist.CLIENT)
public final class HudBarsNeoforge {

    private static final Identifier STAMINA = HudBars.STAMINA_ELEMENT;
    private static final Identifier MANA    = HudBars.MANA_ELEMENT;

    private HudBarsNeoforge() {}

    @SubscribeEvent
    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.FOOD_LEVEL, STAMINA, (graphics, deltaTracker) -> {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null) return;
            if (!HudBars.shouldShowStamina(player)) return;
            int yLineBase = graphics.guiHeight() - mc.gui.rightHeight;
            HudBars.renderStamina(graphics, player, yLineBase);
            mc.gui.rightHeight += HudBars.BAR_HEIGHT;
        });
        event.registerAbove(STAMINA, MANA, (graphics, deltaTracker) -> {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null) return;
            if (!HudBars.shouldShowMana(player)) return;
            int yLineBase = graphics.guiHeight() - mc.gui.rightHeight;
            HudBars.renderMana(graphics, player, yLineBase);
            mc.gui.rightHeight += HudBars.BAR_HEIGHT;
        });
    }
}
