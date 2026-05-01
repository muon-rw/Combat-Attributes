package dev.muon.combat_attributes.damage;

import dev.muon.combat_attributes.attribute.ModAttributes;
import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Static helpers that drive Combat Attributes' damage pipeline. Invoked from a
 * {@code @WrapMethod}-style mixin on {@code LivingEntity#hurtServer}.
 *
 * <p>Damage type routing:
 * <ul>
 *   <li><b>Magic</b>: {@code source.is(c:is_magic)} → magic crit roll.</li>
 *   <li><b>Ranged</b>: {@code is_projectile} AND NOT {@code c:is_magic} → ranged_damage flat
 *       bonus + ranged crit roll.</li>
 *   <li><b>Melee</b>: NOT {@code is_projectile} → melee crit roll. (A magic-tagged direct
 *       hit fires both magic and melee crit; per design that's allowed for now.)</li>
 * </ul>
 *
 * <p>All attribute reads route through {@link ModAttributes#valueOrDefault} so a
 * LivingEntity missing one of our attributes (stale supplier, oddly-registered subclass)
 * degrades to the attribute's default rather than crashing.
 */
public final class DamageHandler {

    private DamageHandler() {}

    /**
     * Pre-damage roll — returns true if the victim dodges and the hit should be
     * cancelled outright. Uses the victim's RNG.
     */
    public static boolean shouldDodge(LivingEntity victim, DamageSource source) {
        double evasion = ModAttributes.valueOrDefault(victim, ModAttributes.evasion());
        if (evasion <= 0.0) return false;
        return victim.getRandom().nextDouble() < evasion;
    }

    /**
     * Modifies incoming damage in place: adds ranged_damage flat bonus on non-magic
     * projectile hits, then applies the appropriate crit multiplier(s).
     */
    public static float modifyIncomingDamage(LivingEntity victim, DamageSource source, float damage) {
        Entity attackerEntity = source.getEntity();
        if (!(attackerEntity instanceof LivingEntity attacker)) return damage;

        boolean magic = source.is(CombatDamageTags.IS_MAGIC);
        boolean projectile = source.is(DamageTypeTags.IS_PROJECTILE);

        // Ranged damage: flat bonus on non-magic projectile hits.
        if (projectile && !magic) {
            damage += (float) ModAttributes.valueOrDefault(attacker, ModAttributes.rangedDamage());
        }

        // Crit rolls — independent per damage classification.
        if (magic) {
            damage = rollCrit(attacker, damage,
                    ModAttributes.magicCritChance(), ModAttributes.magicCritDamage());
        }
        if (projectile && !magic) {
            damage = rollCrit(attacker, damage,
                    ModAttributes.rangedCritChance(), ModAttributes.rangedCritDamage());
        }
        if (!projectile) {
            // Melee covers any non-projectile direct attack — including magic-tagged direct hits.
            damage = rollCrit(attacker, damage,
                    ModAttributes.meleeCritChance(), ModAttributes.meleeCritDamage());
        }

        // Magic defense: armor-style mitigation on incoming magic damage. Applied after crits,
        // mirroring the order vanilla armor sees damage in (post-modifier, pre-hurtServer).
        if (magic) {
            damage = applyMagicDefense(victim, source, damage);
        }

        return damage;
    }

    /**
     * Post-damage hook — runs after vanilla {@code hurtServer} returned true (damage
     * was actually applied). Heals the attacker by {@code damage * lifesteal}, clamped
     * to attackers within their own {@code entity_interaction_range} of the victim.
     */
    public static void afterDamage(LivingEntity victim, DamageSource source, float damage) {
        if (damage <= 0.0F) return;
        Entity attackerEntity = source.getEntity();
        if (!(attackerEntity instanceof LivingEntity attacker)) return;
        if (attacker == victim) return;

        double lifesteal = ModAttributes.valueOrDefault(attacker, ModAttributes.lifesteal());
        if (lifesteal <= 0.0) return;

        double range = attacker.getAttributes().hasAttribute(Attributes.ENTITY_INTERACTION_RANGE)
                ? attacker.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)
                : Attributes.ENTITY_INTERACTION_RANGE.value().getDefaultValue();
        if (attacker.distanceTo(victim) > range) return;

        attacker.heal((float) (damage * lifesteal));
    }

    private static float rollCrit(LivingEntity attacker, float damage,
                                  Holder<Attribute> chanceAttr, Holder<Attribute> damageAttr) {
        double chance = ModAttributes.valueOrDefault(attacker, chanceAttr);
        if (chance <= 0.0) return damage;
        if (attacker.getRandom().nextDouble() >= chance) return damage;
        double mult = ModAttributes.valueOrDefault(attacker, damageAttr);
        return (float) (damage * mult);
    }

    private static float applyMagicDefense(LivingEntity victim, DamageSource source, float damage) {
        double magicDefense = ModAttributes.valueOrDefault(victim, ModAttributes.magicDefense());
        if (magicDefense <= 0.0) return damage;
        // Reuse vanilla armor math directly. Toughness=0 since there's no separate magic toughness
        // attribute; with that, CombatRules degenerates to clamp(magicDefense - damage/2, ...) /25.
        return CombatRules.getDamageAfterAbsorb(victim, damage, source, (float) magicDefense, 0.0F);
    }
}
