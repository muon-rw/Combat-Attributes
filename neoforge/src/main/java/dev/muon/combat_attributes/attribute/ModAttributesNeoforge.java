package dev.muon.combat_attributes.attribute;

import dev.muon.combat_attributes.CombatAttributes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

/**
 * NeoForge-side attribute registration. Drives off {@link ModAttributes#ALL}; populates
 * the common holder map in {@link #init()} after the {@link DeferredRegister} has fired
 * (i.e. once the registry events on the mod event bus have completed).
 *
 * <p>Static initialisation order:
 * <ol>
 *   <li>{@code Configs.register()} runs first (called from {@code CombatAttributes.init()})
 *       so {@link AttributeSpec} suppliers resolve.</li>
 *   <li>{@link #REGISTRY} is registered to the mod event bus in
 *       {@code CombatAttributesNeoforge}'s constructor; entries are recorded eagerly here
 *       but registry objects materialise during the registry phase.</li>
 *   <li>{@link #init()} populates {@link ModAttributes#put}; safe to call from the
 *       {@code @Mod} constructor — {@link DeferredHolder} is itself a {@link Holder}.</li>
 * </ol>
 */
@EventBusSubscriber
public final class ModAttributesNeoforge {

    private ModAttributesNeoforge() {}

    public static final DeferredRegister<Attribute> REGISTRY =
            DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, CombatAttributes.MOD_ID);

    private static final List<DeferredHolder<Attribute, Attribute>> HOLDERS = new ArrayList<>();

    static {
        for (ModAttributes.Entry entry : ModAttributes.ALL) {
            String descriptionId = "attribute." + CombatAttributes.MOD_ID + "." + entry.id();
            HOLDERS.add(REGISTRY.register(entry.id(),
                    () -> new DiminishingRangedAttribute(descriptionId, entry.spec()).setSyncable(true)));
        }
    }

    /** Populate the common holder map. DeferredHolder implements Holder, so this is just a copy. */
    public static void init() {
        for (int i = 0; i < ModAttributes.ALL.size(); i++) {
            ModAttributes.put(ModAttributes.ALL.get(i).id(), HOLDERS.get(i));
        }
    }

    @SubscribeEvent
    public static void attachToLivingEntities(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            for (DeferredHolder<Attribute, Attribute> holder : HOLDERS) {
                event.add(type, holder);
            }
        }
    }
}
