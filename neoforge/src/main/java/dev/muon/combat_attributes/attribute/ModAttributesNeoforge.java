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
import net.neoforged.neoforge.common.PercentageAttribute;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Supplier;

/**
 * NeoForge-side attribute registration. Drives off {@link ModAttributes#ALL}; populates
 * the common holder map in {@link #init()} after the {@link DeferredRegister} has fired.
 *
 * <p>Picks the concrete attribute class from two flags on the {@link ModAttributes.Entry}
 * and its {@link AttributeSpec}:
 * <ul>
 *   <li>percent + diminishing → {@link DiminishingPercentageAttribute}</li>
 *   <li>percent + linear      → {@link PercentageAttribute}</li>
 *   <li>flat    + diminishing → {@link DiminishingRangedAttribute}</li>
 *   <li>flat    + linear      → {@link RangedAttribute}</li>
 * </ul>
 *
 * <p>Percent variants route through NeoForge's {@code IAttributeExtension.toComponent},
 * so vanilla item-attribute tooltips and any third-party UI that uses NeoForge's API
 * picks up the correct percent formatting without further integration.
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
                OptionalDouble percentScale = entry.percentScale();
                Attribute attr;
                if (percentScale.isPresent()) {
                    double scale = percentScale.getAsDouble();
                    attr = snap.diminishing.get()
                            ? new DiminishingPercentageAttribute(descriptionId, entry.spec(), scale)
                            : new PercentageAttribute(descriptionId,
                                    snap.defaultValue.get(), snap.minValue.get(), snap.maxValue.get(), scale);
                } else {
                    attr = snap.diminishing.get()
                            ? new DiminishingRangedAttribute(descriptionId, entry.spec())
                            : new RangedAttribute(descriptionId,
                                    snap.defaultValue.get(), snap.minValue.get(), snap.maxValue.get());
                }
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
