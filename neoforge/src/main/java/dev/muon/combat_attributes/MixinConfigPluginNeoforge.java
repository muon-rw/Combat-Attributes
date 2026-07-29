package dev.muon.combat_attributes;

import com.bawnorton.mixinsquared.adjuster.MixinAnnotationAdjusterRegistrar;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MixinConfigPluginNeoforge implements IMixinConfigPlugin {

    private static final Logger LOGGER = LogManager.getLogger("CombatAttributes-Mixin");

    @Override
    public void onLoad(String mixinPackage) {
        // To set these up, view the MixinSquared wiki:
        // https://github.com/Bawnorton/MixinSquared/wiki
        // MixinAnnotationAdjusterRegistrar.register(new CombatAttributesMixinAdjuster());
        // MixinAnnotationAdjusterRegistrar.register(new CombatAttributesMixinCanceller());
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }


    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.contains(".compat.")) {
            return true;
        }
        List<String> requiredMods = requiredModsFor(mixinClassName);
        for (String modId : requiredMods) {
            if (!isModLoaded(modId)) {
                LOGGER.info("Disabling mixin {} because required mod '{}' is not loaded",
                        getSimpleMixinName(mixinClassName), modId);
                return false;
            }
        }
        if (!requiredMods.isEmpty()) {
            LOGGER.info("Enabling mixin {} - all required mods {} are loaded",
                    getSimpleMixinName(mixinClassName), requiredMods);
        }
        return true;
    }

    // Each path segment under /compat/ is a required mod id, except shared subdirectories.
    private List<String> requiredModsFor(String mixinClassName) {
        String[] parts = mixinClassName.split("\\.");
        Set<String> excludedDirectories = Set.of("client", "accessor");
        List<String> requiredMods = new ArrayList<>();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("compat")) {
                for (int j = i + 1; j < parts.length - 1; j++) {
                    String segment = parts[j];
                    if (!excludedDirectories.contains(segment)) {
                        requiredMods.add(segment);
                    }
                }
                break;
            }
        }
        return requiredMods;
    }

    private String getSimpleMixinName(String mixinClassName) {
        String[] parts = mixinClassName.split("\\.");
        return parts[parts.length - 1];
    }

    private static boolean isModLoaded(String modId) {
        ModList modList = ModList.get();
        if (modList != null) {
            return modList.isLoaded(modId);
        }
        FMLLoader loader = FMLLoader.getCurrentOrNull();
        if (loader == null) {
            return false;
        }
        LoadingModList loadingList = loader.getLoadingModList();
        if (loadingList == null) {
            return false;
        }
        return loadingList.getMods().stream().map(ModInfo::getModId).anyMatch(modId::equals);
    }


    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}