package dev.muon.combat_attributes.client;

import dev.muon.combat_attributes.CombatAttributes;
import dev.muon.combat_attributes.feature.LegacyHunger;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * NeoForge HUD wiring for the stamina + mana bars.
 *
 * <p>Each layer renders relative to {@code Gui.rightHeight}. NeoForge resets
 * that field to 39 every frame and bumps it by 10 after every right-side bar
 * draws (food, vehicle health, etc.) so the air bar above can read it as its
 * top-of-stack offset. Our layers participate by reading {@code rightHeight}
 * for their own Y, then bumping it by 10 only when actually drawn. That single
 * gate is what makes the air bubble layer shift up only while the stamina or
 * mana bar is visible; no mixin required, NeoForge's existing layout is the
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

    private HudBarsNeoforge() {}

    @SubscribeEvent
    public static void register(RegisterGuiLayersEvent event) {
        // Suppress the vanilla hunger row when legacy-hunger is on. Skipping the layer also skips
        // the +10 bump to Gui.rightHeight that vanilla applies inside the food render, so our
        // stamina layer below reads rightHeight=39 and slots into the food row's y.
        event.wrapLayer(VanillaGuiLayers.FOOD_LEVEL, vanilla -> (graphics, deltaTracker) -> {
            if (LegacyHunger.isEnabled()) return;
            vanilla.render(graphics, deltaTracker);
        });
        event.registerAbove(VanillaGuiLayers.FOOD_LEVEL, HudBars.STAMINA_ELEMENT, (graphics, deltaTracker) -> {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null) return;
            if (!HudBars.shouldShowStamina(player)) return;
            int yLineBase = graphics.guiHeight() - mc.gui.rightHeight;
            HudBars.renderStamina(graphics, player, yLineBase);
            mc.gui.rightHeight += HudBars.BAR_HEIGHT;
        });
        event.registerAbove(HudBars.STAMINA_ELEMENT, HudBars.MANA_ELEMENT, (graphics, deltaTracker) -> {
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
