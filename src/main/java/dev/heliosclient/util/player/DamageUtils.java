package dev.heliosclient.util.player;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.PlayerJoinEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.system.mixininterface.IExplosion;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;

import java.util.Objects;

public class DamageUtils implements Listener {
    public static DamageUtils INSTANCE = new DamageUtils();
    static ExplosionBehavior behavior = new ExplosionBehavior();
    private static Explosion explosion;

    public static double calculateBedBlastDamage(Vec3d bedLocation, LivingEntity target) {
        if (target instanceof PlayerEntity && ((PlayerEntity) target).getAbilities().creativeMode) return 0;
        assert HeliosClient.MC.world != null;
        explosion = new Explosion(HeliosClient.MC.world, null, bedLocation.x, bedLocation.y, bedLocation.z, 5, false, Explosion.DestructionType.DESTROY);

        double rawDamage = behavior.calculateDamage(explosion, target);
        rawDamage = calculateReductions(rawDamage, target, HeliosClient.MC.world.getDamageSources().explosion(null));

        if (rawDamage < 0) rawDamage = 0;
        return rawDamage;
    }

    /**
     * Directly taken from:
     * @see LivingEntity#computeFallDamage(float, float)
     */
    public static float calcFallDamage(LivingEntity entity){
      return calcFallDamage(entity,entity.fallDistance);
    }
    /**
     * Directly taken from with some changes:
     * @see LivingEntity#computeFallDamage(float, float)
     */
    public static float calcFallDamage(LivingEntity entity, float fallDistance){
        if (entity.hasStatusEffect(StatusEffects.SLOW_FALLING) || entity.hasStatusEffect(StatusEffects.LEVITATION)) return 0;

        if (entity.getType().isIn(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
            return 0;
        } else {
            StatusEffectInstance statusEffectInstance = entity.getStatusEffect(StatusEffects.JUMP_BOOST);
            float f = statusEffectInstance == null ? 0.0F : (float)(statusEffectInstance.getAmplifier() + 1);

            float fallDamageBeforeRedu = fallDistance - 3.0F - f;

            return (float) Math.ceil(calculateReductions(fallDamageBeforeRedu,entity,entity.getDamageSources().fall()));
        }
    }
    /**
     * Use minecraft's method to calculate damage.
     *
     * @see ExplosionBehavior#calculateDamage(Explosion, Entity)
     */
    public static double calculateCrystalDamage(Vec3d source, PlayerEntity player) {
        if (player == null || player.isCreative()) return 0;
        explosion = new Explosion(HeliosClient.MC.world, null, source.x, source.y, source.z, 6, false, Explosion.DestructionType.DESTROY);
        ((IExplosion) explosion).heliosClient$set(source, 6, false);

        double rawDamage = behavior.calculateDamage(explosion, player);

        rawDamage = calculateReductions(rawDamage, player, HeliosClient.MC.world.getDamageSources().explosion(null));


        if (rawDamage < 0) rawDamage = 0;

        return rawDamage;
    }

    private static double getDamageForDifficulty(double damage) {
        return switch (HeliosClient.MC.world.getDifficulty()) {
            case EASY -> Math.min(damage / 2 + 1, damage);
            case HARD -> damage * (3.0 / 2.0);
            default -> damage;
        };
    }

    /**
     * @see LivingEntity#applyDamage(DamageSource, float)
     */
    public static double calculateReductions(double damage, LivingEntity entity, DamageSource damageSource) {
        if (damageSource.isScaledWithDifficulty()) {
            damage = getDamageForDifficulty(damage);
        }

        // Armor reduction
        damage = DamageUtil.getDamageLeft((float) damage, getArmor(entity), (float) entity.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));

        // Resistance reduction
        damage = resistanceReduction(entity, (float) damage);

        // Protection reduction
        damage = protectionReduction(entity, (float) damage, damageSource);

        return Math.max(damage, 0);
    }

    private static float getArmor(LivingEntity entity) {
        return (float) Math.floor(entity.getAttributeValue(EntityAttributes.GENERIC_ARMOR));
    }

    /**
     * @see LivingEntity#modifyAppliedDamage(DamageSource, float)
     */
    private static float protectionReduction(LivingEntity player, float damage, DamageSource source) {
        int protLevel = EnchantmentHelper.getProtectionAmount(player.getArmorItems(), source);
        return DamageUtil.getInflictedDamage(damage, protLevel);
    }

    /**
     * @see LivingEntity#modifyAppliedDamage(DamageSource, float)
     */
    private static float resistanceReduction(LivingEntity player, float damage) {
        StatusEffectInstance resistance = player.getStatusEffect(StatusEffects.RESISTANCE);
        if (resistance != null) {
            int lvl = resistance.getAmplifier() + 1;
            damage *= (1 - (lvl * 0.2f));
        }

        return Math.max(damage, 0);
    }

    public static float calculateSwordDamage(ItemStack sword, PlayerEntity attacker, LivingEntity target) {
        float swordDamage = 0;
        try {
            swordDamage = ((SwordItem) sword.getItem()).getAttackDamage() + 1.0F;
        } catch (ClassCastException e) {
            return 0;
        }
        // Apply Sharpness enchantment
        int sharpnessLevel = EnchantmentHelper.getLevel(Enchantments.SHARPNESS, sword);
        swordDamage += sharpnessLevel * 1.25F; // Each level of Sharpness adds 1.25 damage

        // Apply Strength effect
        if (attacker.hasStatusEffect(StatusEffects.STRENGTH)) {
            int strengthLevel = Objects.requireNonNull(attacker.getStatusEffect(StatusEffects.STRENGTH)).getAmplifier() + 1;
            swordDamage += strengthLevel * 3.0F; // Each level of Strength adds 3 damage
        }

        swordDamage = (float) calculateReductions(swordDamage, target, HeliosClient.MC.world.getDamageSources().playerAttack(attacker));

        return swordDamage;
    }

    @SubscribeEvent
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        explosion = new Explosion(HeliosClient.MC.world, null, 0, 0, 0, 6, false, Explosion.DestructionType.DESTROY);
    }
}
