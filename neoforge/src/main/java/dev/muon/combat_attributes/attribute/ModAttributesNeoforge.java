package dev.muon.combat_attributes.attribute;

import dev.muon.combat_attributes.CombatAttributes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * NeoForge-side attribute registration. Drives off {@link ModAttributes#ALL}; populates
 * the common holder map in {@link #init()} after the {@link DeferredRegister} has fired.
 *
 * <p>Reads {@code AttributeSpec.diminishing} per entry and registers either a
 * {@link DiminishingRangedAttribute} or vanilla {@link RangedAttribute}. Future change:
 * when the percent-display path is wired on this loader, swap the {@code RangedAttribute}
 * for {@code PercentageAttribute} on percent-flagged entries.
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
            Supplier<Attribute> factory = () -> {
                AttributeSpec snap = entry.spec().get();
                Attribute attr = snap.diminishing.get()
                        ? new DiminishingRangedAttribute(descriptionId, entry.spec())
                        : new RangedAttribute(descriptionId,
                                snap.defaultValue.get(), snap.minValue.get(), snap.maxValue.get());
                attr.setSyncable(true);
                return attr;
            };
            HOLDERS.add(REGISTRY.register(entry.id(), factory));
        }
    }

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
