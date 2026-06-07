package dev.muon.combat_attributes.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.muon.combat_attributes.CombatAttributes;
import dev.muon.combat_attributes.config.Configs;
import dev.muon.combat_attributes.resource.PlayerResources;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

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

    // Maps current/max into 20 half-pips so a 200-max bar reads like a 20-max one.
    // Pips fill right-to-left to mirror the hunger bar, which drains from the left edge.
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
            int pipX = xRight - i * PIP_STRIDE - SPRITE_SIZE;
            graphics.blitSprite(pipeline, empty, pipX, yLineBase, SPRITE_SIZE, SPRITE_SIZE);
            int halfIndex = i * 2 + 1;
            if (halfIndex < filledHalves) {
                graphics.blitSprite(pipeline, full, pipX, yLineBase, SPRITE_SIZE, SPRITE_SIZE);
            } else if (halfIndex == filledHalves) {
                graphics.blitSprite(pipeline, half, pipX, yLineBase, SPRITE_SIZE, SPRITE_SIZE);
            }
        }
    }
}
