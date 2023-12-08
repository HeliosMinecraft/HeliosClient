package dev.heliosclient.util;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.PlayerJoinEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.util.interfaces.IExplosion;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.Objects;

public class DamageUtils implements Listener {
    private static final float MAX_DAMAGE = 100.0F; // Maximum damage value
    private static Explosion explosion;

    public static float calculateMaxDamage(World world, Vec3d source, PlayerEntity player) {
        double exposure = getExposure(world, source, player);
        float rawDamage = (float) (exposure * MAX_DAMAGE);

        // Calculate damage reductions
        float armorReduction = getArmorReduction(player);
        ((IExplosion) explosion).set(source, 6, false);
        float blastProtectionReduction = getBlastProtectionReduction(player, explosion);
        float protectionReduction = getProtectionReduction(player);

        // Calculate final damage

        return rawDamage * (1.0F - armorReduction) * (1.0F - blastProtectionReduction) * (1.0F - protectionReduction);
    }

    public static double calculateBedBlastDamage(LivingEntity target, Vec3d bedLocation) {
        if (target instanceof PlayerEntity && ((PlayerEntity) target).getAbilities().creativeMode) return 0;
        explosion = new Explosion(HeliosClient.MC.world, null, 0, 0, 0, 6, false, Explosion.DestructionType.DESTROY);

        double distance = Math.sqrt(target.squaredDistanceTo(bedLocation));
        if (distance > 10) return 0;

        double exposure = Explosion.getExposure(bedLocation, target);
        double impactFactor = (1.0 - (distance / 10.0)) * exposure;
        double rawDamage = (impactFactor * impactFactor + impactFactor) / 2 * 7 * (5 * 2) + 1;

        // Adjust damage based on difficulty
        rawDamage = getDamageForDifficulty(rawDamage);

        // Reduce damage based on resistance
        rawDamage = getResistanceReduction(target, rawDamage);

        // Reduce damage based on armor
        rawDamage = DamageUtil.getDamageLeft((float) rawDamage, (float) target.getArmor(), (float) Objects.requireNonNull(target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)).getValue());

        // Reduce damage based on blast protection enchantment
        ((IExplosion) explosion).set(bedLocation, 5, true);
        rawDamage = getBlastProtectionReduction(target, rawDamage, explosion);

        if (rawDamage < 0) rawDamage = 0;
        return rawDamage;
    }

    public static float calculateCrystalDamage(World world, Vec3d source, PlayerEntity player) {
        if (player == null || player.isCreative()) return 0;
        explosion = new Explosion(HeliosClient.MC.world, null, 0, 0, 0, 6, false, Explosion.DestructionType.DESTROY);

        double distance = source.distanceTo(player.getPos());
        double exposure = getExposure(world, source, player);
        double impactFactor = (1.0 - (distance / 10.0)) * exposure;

        // Calculate raw damage based on impact factor
        float rawDamage = (float) ((impactFactor * impactFactor + impactFactor) / 2 * 7 * (5 * 2) + 1);

        // Calculate damage reductions
        float armorReduction = getArmorReduction(player);
        ((IExplosion) explosion).set(source, 6, false);
        float blastProtectionReduction = getBlastProtectionReduction(player, explosion);
        float protectionReduction = getProtectionReduction(player);

        // Subtract reductions from raw damage
        float finalDamage = rawDamage - armorReduction - blastProtectionReduction - protectionReduction;

        if (finalDamage < 0) finalDamage = 0;

        return finalDamage;
    }

    private static double getDamageForDifficulty(double damage) {
        return switch (HeliosClient.MC.world.getDifficulty()) {
            case PEACEFUL -> 0;
            case EASY -> Math.min(damage / 2 + 1, damage);
            case HARD -> damage * 3 / 2;
            default -> damage;
        };
    }

    private static double getProtectionReduction(Entity entity, double damage) {
        int protLevel = EnchantmentHelper.getProtectionAmount(entity.getArmorItems(), HeliosClient.MC.world.getDamageSources().generic());
        if (protLevel > 20) protLevel = 20;

        damage *= 1 - (protLevel / 25.0);
        return damage < 0 ? 0 : damage;
    }

    private static double getBlastProtectionReduction(Entity player, double damage, Explosion explosion) {
        int protLevel = EnchantmentHelper.getProtectionAmount(player.getArmorItems(), HeliosClient.MC.world.getDamageSources().explosion((explosion)));
        if (protLevel > 20) protLevel = 20;

        damage *= 1 - (protLevel / 25.0);
        return damage < 0 ? 0 : damage;
    }

    private static double getResistanceReduction(LivingEntity player, double damage) {
        if (player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            int lvl = (player.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1);
            damage *= (1 - (lvl * 0.2));
        }

        return damage < 0 ? 0 : damage;
    }

    private static float getArmorReduction(LivingEntity entity) {
        int armorValue = entity.getArmor();
        return (float) armorValue * 0.04F; // Each point of armor reduces damage by 4%
    }

    private static float getBlastProtectionReduction(LivingEntity entity, Explosion explosion) {
        int blastProtectionLevel = EnchantmentHelper.getProtectionAmount(entity.getArmorItems(), HeliosClient.MC.world.getDamageSources().explosion(explosion));

        return (float) blastProtectionLevel * 0.15F; // Each level of Blast Protection reduces explosion damage by an additional 15%
    }

    private static float getProtectionReduction(LivingEntity entity) {
        int protectionLevel = EnchantmentHelper.getLevel(Enchantments.PROTECTION, entity.getActiveItem());
        return (float) protectionLevel * 0.04F; // Each level of Protection reduces all damage by an additional 4%
    }

    private static float getResistanceReduction(LivingEntity entity) {
        if (entity.hasStatusEffect(StatusEffects.RESISTANCE)) {
            int resistanceLevel = entity.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1;
            return resistanceLevel * 0.20F; // Each level of Resistance reduces all damage by an additional 20%
        } else {
            return 0.0F;
        }
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

        // Apply damage reductions
        float armorReduction = getArmorReduction(target);
        float protectionReduction = getProtectionReduction(target);

        // Calculate final damage

        return swordDamage * (1.0F - armorReduction) * (1.0F - protectionReduction);
    }

    private static double getExposure(World world, Vec3d source, Entity entity) {
        Box box = entity.getBoundingBox();
        int totalRays = 0;
        int unobstructedRays = 0;

        for (double x = box.minX; x < box.maxX; x += 1.0D) {
            for (double y = box.minY; y < box.maxY; y += 1.0D) {
                for (double z = box.minZ; z < box.maxZ; z += 1.0D) {
                    totalRays++;
                    Vec3d target = new Vec3d(x, y, z);
                    RaycastContext context = new RaycastContext(source, target, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity);
                    if (world.raycast(context).getType() == HitResult.Type.MISS) {
                        unobstructedRays++;
                    }
                }
            }
        }

        return (double) unobstructedRays / totalRays;
    }

    @SubscribeEvent
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        explosion = new Explosion(HeliosClient.MC.world, null, 0, 0, 0, 6, false, Explosion.DestructionType.DESTROY);
    }
}
