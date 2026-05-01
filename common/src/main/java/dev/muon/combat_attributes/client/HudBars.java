package dev.muon.combat_attributes.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.muon.combat_attributes.CombatAttributes;
import dev.muon.combat_attributes.config.Configs;
import dev.muon.combat_attributes.resource.PlayerResources;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/**
 * Common HUD rendering for stamina + mana bars. Both loaders share one
 * pip-drawing routine and one visibility predicate; only the layer registration
 * differs (see {@code HudBarsFabric} / {@code HudBarsNeoforge}).
 *
 * <p>Each bar shows 10 pips, vanilla hunger-row sized (9×9 px sprites, stride
 * 8 px, right-aligned to {@code guiWidth/2 + 91}). The pip count represents the
 * pool's fill ratio — 1 pip = 10% of max — so a 200-max stamina bar reads the
 * same as a 20-max one. Each pip resolves to full / half / empty by mapping
 * {@code current/max} into 20 half-pips.
 *
 * <p>Element identifiers are exposed so the loader-specific registration can
 * use them as both element IDs (for {@code HudElementRegistry} /
 * {@code RegisterGuiLayersEvent}) and lookup keys for Fabric's height registry.
 *
 * <p>Sprite paths match the existing {@code stamina.png} / {@code stamina_empty.png}
 * / {@code stamina_half.png} files under {@code assets/combat_attributes/textures/gui/sprites/hud/}
 * — atlas resolution picks them up automatically without a metadata file.
 */
public final class HudBars {

    public static final Identifier STAMINA_ELEMENT = Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, "stamina_bar");
    public static final Identifier MANA_ELEMENT    = Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, "mana_bar");

    public static final Identifier STAMINA_FULL_SPRITE  = Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, "hud/stamina");
    public static final Identifier STAMINA_HALF_SPRITE  = Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, "hud/stamina_half");
    public static final Identifier STAMINA_EMPTY_SPRITE = Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, "hud/stamina_empty");

    public static final Identifier MANA_FULL_SPRITE  = Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, "hud/mana");
    public static final Identifier MANA_HALF_SPRITE  = Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, "hud/mana_half");
    public static final Identifier MANA_EMPTY_SPRITE = Identifier.fromNamespaceAndPath(CombatAttributes.MOD_ID, "hud/mana_empty");

    public static final int BAR_HEIGHT = 10;
    public static final int SPRITE_SIZE = 9;
    public static final int PIP_STRIDE = 8;
    private static final int PIPS = 10;

    private HudBars() {}

    public static boolean shouldShowStamina(Player player) {
        if (player == null || player.isSpectator() || player.isCreative()) return false;
        float max = PlayerResources.getMaxStamina(player);
        if (max <= 0.0F) return false;
        if (Configs.GENERAL == null || !Configs.GENERAL.hideStaminaWhenFull.get()) return true;
        return PlayerResources.get(player).stamina() < max;
    }

    public static boolean shouldShowMana(Player player) {
        if (player == null || player.isSpectator() || player.isCreative()) return false;
        float max = PlayerResources.getMaxMana(player);
        if (max <= 0.0F) return false;
        if (Configs.GENERAL == null || !Configs.GENERAL.hideManaWhenFull.get()) return true;
        return PlayerResources.get(player).mana() < max;
    }

    public static void renderStamina(GuiGraphicsExtractor graphics, Player player, int yLineBase) {
        if (player == null || player.isSpectator() || player.isCreative()) return;
        float max = PlayerResources.getMaxStamina(player);
        if (max <= 0.0F) return;
        float current = PlayerResources.get(player).stamina();
        if (Configs.GENERAL != null && Configs.GENERAL.hideStaminaWhenFull.get() && current >= max) return;
        int xRight = graphics.guiWidth() / 2 + 91;
        renderBar(graphics, xRight, yLineBase, current, max,
                STAMINA_FULL_SPRITE, STAMINA_HALF_SPRITE, STAMINA_EMPTY_SPRITE);
    }

    public static void renderMana(GuiGraphicsExtractor graphics, Player player, int yLineBase) {
        if (player == null || player.isSpectator() || player.isCreative()) return;
        float max = PlayerResources.getMaxMana(player);
        if (max <= 0.0F) return;
        float current = PlayerResources.get(player).mana();
        if (Configs.GENERAL != null && Configs.GENERAL.hideManaWhenFull.get() && current >= max) return;
        int xRight = graphics.guiWidth() / 2 + 91;
        renderBar(graphics, xRight, yLineBase, current, max,
                MANA_FULL_SPRITE, MANA_HALF_SPRITE, MANA_EMPTY_SPRITE);
    }

    /**
     * Renders a 10-pip status bar at {@code (xRight, yLineBase)} matching the
     * vanilla hunger row's geometry. Pips fill right-to-left to mirror the
     * hunger bar, which the player's eye expects to drain from the left edge.
     */
    private static void renderBar(GuiGraphicsExtractor graphics, int xRight, int yLineBase,
                                  float current, float max,
                                  Identifier full, Identifier half, Identifier empty) {
        int filledHalves;
        if (max <= 0.0F) {
            filledHalves = 0;
        } else {
            float clamped = Math.max(0.0F, Math.min(current, max));
            filledHalves = Math.round((clamped / max) * (PIPS * 2));
        }
        RenderPipeline pipeline = RenderPipelines.GUI_TEXTURED;
        for (int i = 0; i < PIPS; i++) {
            int xo = xRight - i * PIP_STRIDE - SPRITE_SIZE;
            graphics.blitSprite(pipeline, empty, xo, yLineBase, SPRITE_SIZE, SPRITE_SIZE);
            int halfIndex = i * 2 + 1;
            if (halfIndex < filledHalves) {
                graphics.blitSprite(pipeline, full, xo, yLineBase, SPRITE_SIZE, SPRITE_SIZE);
            } else if (halfIndex == filledHalves) {
                graphics.blitSprite(pipeline, half, xo, yLineBase, SPRITE_SIZE, SPRITE_SIZE);
            }
        }
    }
}
