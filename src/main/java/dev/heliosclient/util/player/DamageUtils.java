package dev.heliosclient.util.player;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.movement.NoFall;
import dev.heliosclient.util.entity.EntityUtils;
import dev.heliosclient.util.world.ChunkUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.explosion.ExplosionImpl;

public class DamageUtils implements Listener {
    public static DamageUtils INSTANCE = new DamageUtils();
    //Explosion power
    public static int BED_POWER = 5, CRYSTAL_POWER = 6;

    public static double calculateBedBlastDamage(Vec3d bedLocation, LivingEntity target) {
        if (target instanceof PlayerEntity && ((PlayerEntity) target).getAbilities().creativeMode) return 0;
        assert HeliosClient.MC.world != null;

        double rawDamage = ExplosionImpl.calculateReceivedDamage(bedLocation,target);
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
        double rawDamage = ExplosionImpl.calculateReceivedDamage(source,player);

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
        damage = DamageUtil.getDamageLeft(entity,(float) damage,damageSource ,getArmor(entity),(float) entity.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS));

        // Resistance reduction
        damage = resistanceReduction(entity, (float) damage);

        // Protection reduction
        damage = protectionReduction(entity, (float) damage, damageSource);

        return Math.max(damage, 0);
    }

    private static float getArmor(LivingEntity entity) {
        return (float) Math.floor(entity.getAttributeValue(EntityAttributes.ARMOR));
    }

    /**
     * @see LivingEntity#modifyAppliedDamage(DamageSource, float)
     */
    private static float protectionReduction(LivingEntity player, float damage, DamageSource source) {
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) return damage;

        int damageProtection = 0;

        for (ItemStack stack : player.getAllArmorItems()) {
            Object2IntMap<RegistryKey<Enchantment>> enchantments = new Object2IntOpenHashMap<>();
            for(RegistryEntry<Enchantment> ench: stack.getEnchantments().getEnchantments()){
                RegistryKey<Enchantment> enchantment = ench.getKey().get();
                enchantments.put(enchantment, stack.getEnchantments().getLevel(ench));
            }


            int protection = enchantments.getInt(Enchantments.PROTECTION);
            if (protection > 0) {
                damageProtection += protection;
            }

            int fireProtection = enchantments.getInt(Enchantments.FIRE_PROTECTION);
            if (fireProtection > 0 && source.isIn(DamageTypeTags.IS_FIRE)) {
                damageProtection += 2 * fireProtection;
            }

            int blastProtection = enchantments.getInt(Enchantments.BLAST_PROTECTION);
            if (blastProtection > 0 && source.isIn(DamageTypeTags.IS_EXPLOSION)) {
                damageProtection += 2 * blastProtection;
            }

            int projectileProtection = enchantments.getInt(Enchantments.PROJECTILE_PROTECTION);
            if (projectileProtection > 0 && source.isIn(DamageTypeTags.IS_PROJECTILE)) {
                damageProtection += 2 * projectileProtection;
            }

            int featherFalling = enchantments.getInt(Enchantments.FEATHER_FALLING);
            if (featherFalling > 0 && source.isIn(DamageTypeTags.IS_FALL)) {
                damageProtection += 3 * featherFalling;
            }
        }

        return DamageUtil.getInflictedDamage(damage, damageProtection);
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


    public static float calculateDamageByEnv(){
        float totalDamage = 0;

        for(Entity e: EntityUtils.getAllOfType(CRYSTAL_POWER + 2, EntityType.END_CRYSTAL)){
            double crystalDamage = calculateCrystalDamage(e.getPos(),HeliosClient.MC.player);
            totalDamage = (float) Math.max(crystalDamage,totalDamage);
        }

        if (!HeliosClient.MC.world.getDimensionEntry().matchesKey(DimensionTypes.OVERWORLD)) {
            for(BlockEntity blockEntity : ChunkUtils.getBlockEntityStreamInChunks().filter(bE -> bE instanceof BedBlockEntity).toList()) {
                float damage = (float) calculateBedBlastDamage(blockEntity.getPos().toCenterPos(),HeliosClient.MC.player);
                totalDamage = Math.max(damage,totalDamage);
            }
        }

        if(!ModuleManager.get(NoFall.class).isActive()){
            totalDamage = Math.max(calcFallDamage(HeliosClient.MC.player),totalDamage);
        }

        return totalDamage;
    }
}
