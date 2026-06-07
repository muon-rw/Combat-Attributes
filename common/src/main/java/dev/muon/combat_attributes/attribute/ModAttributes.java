package dev.muon.combat_attributes.attribute;

import dev.muon.combat_attributes.config.Configs;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attribute.Sentiment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.Supplier;

/**
 * Read attributes via {@link #valueOrDefault} so an entity whose {@code AttributeSupplier}
 * lacks one (stale supplier from an older version, or a subclass that skips
 * {@code createLivingAttributes}) degrades to the attribute's default instead of throwing.
 */
public final class ModAttributes {

    private ModAttributes() {}

    /**
     * {@code percentScale} drives percent-display tooltips and is consumed differently per
     * loader: Fabric publishes it to Dynamic Tooltips; NeoForge selects the percent attribute
     * class at registration.
     */
    public record Entry(String id, Supplier<AttributeSpec> spec, OptionalDouble percentScale,
                        boolean playerOnly, Sentiment sentiment) {
        public static Entry percent(String id, Supplier<AttributeSpec> spec, double scale) {
            return new Entry(id, spec, OptionalDouble.of(scale), false, Sentiment.POSITIVE);
        }
        public static Entry flat(String id, Supplier<AttributeSpec> spec) {
            return new Entry(id, spec, OptionalDouble.empty(), false, Sentiment.POSITIVE);
        }
        public static Entry playerFlat(String id, Supplier<AttributeSpec> spec) {
            return new Entry(id, spec, OptionalDouble.empty(), true, Sentiment.POSITIVE);
        }
        public static Entry playerPercent(String id, Supplier<AttributeSpec> spec, double scale) {
            return new Entry(id, spec, OptionalDouble.of(scale), true, Sentiment.POSITIVE);
        }
        /** Percent entry where lower values are the buff (e.g. {@code mana_cost}); flips tooltip colour via {@link Attribute.Sentiment#NEGATIVE}. */
        public static Entry playerPercentNegative(String id, Supplier<AttributeSpec> spec, double scale) {
            return new Entry(id, spec, OptionalDouble.of(scale), true, Sentiment.NEGATIVE);
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
            Entry.flat   ("magic_power",        () -> Configs.ATTRIBUTES.magicPower),
            // Defensive
            Entry.percent("evasion",            () -> Configs.ATTRIBUTES.evasion,          100.0),
            Entry.percent("lifesteal",          () -> Configs.ATTRIBUTES.lifesteal,        100.0),
            Entry.flat   ("magic_defense",      () -> Configs.ATTRIBUTES.magicDefense),
            Entry.flat   ("health_regen",       () -> Configs.ATTRIBUTES.healthRegen),
            // Bow physics
            Entry.percent("draw_speed",         () -> Configs.ATTRIBUTES.drawSpeed,        100.0),
            Entry.percent("arrow_velocity",     () -> Configs.ATTRIBUTES.arrowVelocity,    100.0),
            Entry.percent("accuracy",           () -> Configs.ATTRIBUTES.accuracy,         100.0),
            // Player resources
            Entry.playerFlat   ("max_stamina",    () -> Configs.ATTRIBUTES.maxStamina),
            Entry.playerFlat   ("stamina_regen",  () -> Configs.ATTRIBUTES.staminaRegen),
            Entry.playerPercentNegative("stamina_cost", () -> Configs.ATTRIBUTES.staminaCost, 100.0),
            Entry.playerFlat   ("max_mana",       () -> Configs.ATTRIBUTES.maxMana),
            Entry.playerFlat   ("mana_regen",     () -> Configs.ATTRIBUTES.manaRegen),
            Entry.playerPercentNegative("mana_cost", () -> Configs.ATTRIBUTES.manaCost,    100.0),
            // Player progression
            Entry.playerPercent("experience_gain",() -> Configs.ATTRIBUTES.experienceGain, 100.0)
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
    public static Holder<Attribute> magicPower()       { return get("magic_power"); }
    public static Holder<Attribute> evasion()          { return get("evasion"); }
    public static Holder<Attribute> lifesteal()        { return get("lifesteal"); }
    public static Holder<Attribute> magicDefense()     { return get("magic_defense"); }
    public static Holder<Attribute> healthRegen() { return get("health_regen"); }
    public static Holder<Attribute> drawSpeed()        { return get("draw_speed"); }
    public static Holder<Attribute> arrowVelocity()    { return get("arrow_velocity"); }
    public static Holder<Attribute> accuracy()         { return get("accuracy"); }
    public static Holder<Attribute> maxStamina()       { return get("max_stamina"); }
    public static Holder<Attribute> staminaRegen()     { return get("stamina_regen"); }
    public static Holder<Attribute> staminaCost()      { return get("stamina_cost"); }
    public static Holder<Attribute> maxMana()          { return get("max_mana"); }
    public static Holder<Attribute> manaRegen()        { return get("mana_regen"); }
    public static Holder<Attribute> manaCost()         { return get("mana_cost"); }
    public static Holder<Attribute> experienceGain()   { return get("experience_gain"); }
}
