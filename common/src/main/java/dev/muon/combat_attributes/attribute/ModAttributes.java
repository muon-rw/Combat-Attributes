package dev.muon.combat_attributes.attribute;

import dev.muon.combat_attributes.config.Configs;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.Supplier;

/**
 * Common attribute registry for the mod. Lists every attribute once in {@link #ALL};
 * loader-specific code iterates that list to register attributes and to attach them
 * to every living entity, populating the holder map via {@link #put}.
 *
 * <p>Lookups go through {@link #get(String)} or the typed convenience accessors
 * (e.g. {@link #critChance()}); both throw if the attribute hasn't been registered yet.
 */
public final class ModAttributes {

    private ModAttributes() {}

    /**
     * Pairs an attribute id with its {@link AttributeSpec} and (optionally) a percent-display
     * scale factor. When {@code percentScale} is present:
     * <ul>
     *   <li>On Fabric: passed to {@code DynamicTooltipsAPI.declarePercentAttribute} at init.</li>
     *   <li>On NeoForge: a future swap of the concrete class to {@code PercentageAttribute}
     *       can read it as the {@code scaleFactor} constructor argument.</li>
     * </ul>
     */
    public record Entry(String id, Supplier<AttributeSpec> spec, OptionalDouble percentScale) {
        public static Entry flat(String id, Supplier<AttributeSpec> spec) {
            return new Entry(id, spec, OptionalDouble.empty());
        }

        public static Entry percent(String id, Supplier<AttributeSpec> spec, double scale) {
            return new Entry(id, spec, OptionalDouble.of(scale));
        }
    }

    public static final List<Entry> ALL = List.of(
            Entry.percent("crit_chance", () -> Configs.ATTRIBUTES.critChance, 100.0),
            Entry.flat   ("crit_damage", () -> Configs.ATTRIBUTES.critDamage),
            Entry.percent("lifesteal",   () -> Configs.ATTRIBUTES.lifesteal,  100.0)
    );

    private static final Map<String, Holder<Attribute>> HOLDERS = new HashMap<>();

    public static void put(String id, Holder<Attribute> holder) {
        HOLDERS.put(id, holder);
    }

    public static Holder<Attribute> get(String id) {
        Holder<Attribute> h = HOLDERS.get(id);
        if (h == null) throw new IllegalStateException("Combat attribute '" + id + "' not registered");
        return h;
    }

    public static Iterable<Holder<Attribute>> allHolders() {
        return HOLDERS.values();
    }

    public static Holder<Attribute> critChance() { return get("crit_chance"); }
    public static Holder<Attribute> critDamage() { return get("crit_damage"); }
    public static Holder<Attribute> lifesteal()  { return get("lifesteal"); }
}
