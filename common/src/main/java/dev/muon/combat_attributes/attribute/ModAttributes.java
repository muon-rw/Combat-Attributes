package dev.muon.combat_attributes.attribute;

import dev.muon.combat_attributes.config.Configs;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
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
 * <p>How an attribute stacks is decided at registration time from
 * {@code AttributeSpec.stackingMode} — exposed in the config file as a per-attribute enum
 * ({@code LINEAR} / {@code SOFT_CAP} / {@code PROBABILISTIC}). Loader code reads it and
 * chooses the concrete attribute class: vanilla {@code RangedAttribute} for {@code LINEAR},
 * {@code DiminishingRangedAttribute} (or the percent variant on NeoForge) for the others.
 *
 * <p>All attribute reads from this mod's gameplay code should go through
 * {@link #valueOrDefault(LivingEntity, Holder)} so that a partial-registration edge case
 * (e.g. an entity loaded with a stale {@code AttributeSupplier} from a previous version,
 * or a third-party LivingEntity subclass that doesn't chain through
 * {@code createLivingAttributes}) degrades gracefully to the attribute's intrinsic
 * default rather than throwing {@code IllegalArgumentException}.
 */
public final class ModAttributes {

    private ModAttributes() {}

    /**
     * Pairs an attribute id with its {@link AttributeSpec} supplier and an optional
     * percent-display scale factor. Loader code consumes the scale: Fabric publishes it
     * to Dynamic Tooltips at client init; NeoForge picks {@code PercentageAttribute} /
     * {@code DiminishingPercentageAttribute} as the concrete class at registration time.
     */
    public record Entry(String id, Supplier<AttributeSpec> spec, OptionalDouble percentScale) {
        public static Entry percent(String id, Supplier<AttributeSpec> spec, double scale) {
            return new Entry(id, spec, OptionalDouble.of(scale));
        }
        public static Entry flat(String id, Supplier<AttributeSpec> spec) {
            return new Entry(id, spec, OptionalDouble.empty());
        }
    }

    public static final List<Entry> ALL = List.of(
            // Melee
            Entry.percent("melee_crit_chance",  () -> Configs.ATTRIBUTES.meleeCritChance,  100.0),
            Entry.percent("melee_crit_damage",  () -> Configs.ATTRIBUTES.meleeCritDamage,  100.0),
            // Ranged
            Entry.flat   ("ranged_damage",      () -> Configs.ATTRIBUTES.rangedDamage),
            Entry.percent("ranged_crit_chance", () -> Configs.ATTRIBUTES.rangedCritChance, 100.0),
            Entry.percent("ranged_crit_damage", () -> Configs.ATTRIBUTES.rangedCritDamage, 100.0),
            // Magic
            Entry.percent("magic_crit_chance",  () -> Configs.ATTRIBUTES.magicCritChance,  100.0),
            Entry.percent("magic_crit_damage",  () -> Configs.ATTRIBUTES.magicCritDamage,  100.0),
            // Defensive
            Entry.percent("evasion",            () -> Configs.ATTRIBUTES.evasion,          100.0),
            Entry.percent("lifesteal",          () -> Configs.ATTRIBUTES.lifesteal,        100.0),
            // Bow physics
            Entry.percent("draw_speed",         () -> Configs.ATTRIBUTES.drawSpeed,        100.0),
            Entry.percent("arrow_velocity",     () -> Configs.ATTRIBUTES.arrowVelocity,    100.0),
            Entry.percent("accuracy",           () -> Configs.ATTRIBUTES.accuracy,         100.0)
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

    /**
     * Reads an attribute value safely — returns the attribute's intrinsic default if the
     * entity's {@code AttributeSupplier} doesn't include this attribute, instead of
     * throwing. Use this anywhere this mod's code reads its own attributes off a
     * LivingEntity that might predate the attribute's registration (e.g. a saved entity
     * loaded after a mod update that added new attributes, or an entity whose subclass
     * doesn't chain through {@code LivingEntity#createLivingAttributes}).
     */
    public static double valueOrDefault(LivingEntity entity, Holder<Attribute> attr) {
        return entity.getAttributes().hasAttribute(attr)
                ? entity.getAttributeValue(attr)
                : attr.value().getDefaultValue();
    }

    public static Holder<Attribute> meleeCritChance()  { return get("melee_crit_chance"); }
    public static Holder<Attribute> meleeCritDamage()  { return get("melee_crit_damage"); }
    public static Holder<Attribute> rangedDamage()     { return get("ranged_damage"); }
    public static Holder<Attribute> rangedCritChance() { return get("ranged_crit_chance"); }
    public static Holder<Attribute> rangedCritDamage() { return get("ranged_crit_damage"); }
    public static Holder<Attribute> magicCritChance()  { return get("magic_crit_chance"); }
    public static Holder<Attribute> magicCritDamage()  { return get("magic_crit_damage"); }
    public static Holder<Attribute> evasion()          { return get("evasion"); }
    public static Holder<Attribute> lifesteal()        { return get("lifesteal"); }
    public static Holder<Attribute> drawSpeed()        { return get("draw_speed"); }
    public static Holder<Attribute> arrowVelocity()    { return get("arrow_velocity"); }
    public static Holder<Attribute> accuracy()         { return get("accuracy"); }
}
