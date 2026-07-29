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

public final class DamageHandler {

    private DamageHandler() {}

    public static boolean shouldDodge(LivingEntity victim, DamageSource source) {
        double evasion = ModAttributes.valueOrDefault(victim, ModAttributes.evasion());
        if (evasion <= 0.0) return false;
        return victim.getRandom().nextDouble() < evasion;
    }

    public static float modifyIncomingDamage(LivingEntity victim, DamageSource source, float damage) {
        Entity attackerEntity = source.getEntity();
        if (!(attackerEntity instanceof LivingEntity attacker)) return damage;

        boolean magic = source.is(CombatDamageTags.IS_MAGIC);
        boolean projectile = source.is(DamageTypeTags.IS_PROJECTILE);

        damage = applyRangedDamageBonus(attacker, magic, projectile, damage);
        damage = applyCritByDamageType(attacker, magic, projectile, damage);
        if (magic) {
            damage = applyMagicDefense(victim, source, damage);
        }
        return damage;
    }

    private static float applyRangedDamageBonus(LivingEntity attacker, boolean magic, boolean projectile, float damage) {
        if (projectile && !magic) {
            damage += (float) ModAttributes.valueOrDefault(attacker, ModAttributes.rangedDamage());
        }
        return damage;
    }

    private static float applyCritByDamageType(LivingEntity attacker, boolean magic, boolean projectile, float damage) {
        if (magic) {
            damage = rollCrit(attacker, damage,
                    ModAttributes.magicCritChance(), ModAttributes.magicCritDamage());
        }
        if (projectile && !magic) {
            damage = rollCrit(attacker, damage,
                    ModAttributes.rangedCritChance(), ModAttributes.rangedCritDamage());
        }
        if (!projectile) {
            // Magic-tagged direct (non-projectile) hits also roll a melee crit; intentional.
            damage = rollCrit(attacker, damage,
                    ModAttributes.meleeCritChance(), ModAttributes.meleeCritDamage());
        }
        return damage;
    }

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
