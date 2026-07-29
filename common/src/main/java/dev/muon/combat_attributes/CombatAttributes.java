package dev.muon.combat_attributes;

import dev.muon.combat_attributes.config.Configs;
import dev.muon.combat_attributes.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CombatAttributes {

    public static final String MOD_ID = "combat_attributes";
    public static final String MOD_NAME = "CombatAttributes";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    public Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
  public static void init() {

        Configs.register();
        if (Services.PLATFORM.isModLoaded(MOD_ID)) {
            LOG.info("Hello to template");
        }
    }
}