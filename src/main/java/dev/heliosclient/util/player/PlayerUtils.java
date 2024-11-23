package dev.heliosclient.util.player;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.mixin.AccessorMinecraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.*;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static dev.heliosclient.util.color.ColorUtils.blend;

public class PlayerUtils {
    public static MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean canSeeEntity(Entity entity) {
        Vec3d playerEyePos = mc.player.getEyePos();
        Box entityBox = entity.getBoundingBox();
        return mc.player.getWorld().raycast(new RaycastContext(playerEyePos, entityBox.getCenter(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player)).getType() == HitResult.Type.MISS;
    }

    public static boolean isPlayerNearEdge(double edgeThreshold) {
        Vec2f safeWalkMotion = MovementUtils.performSafeMovement(mc.player.input.movementForward,mc.player.input.movementSideways,edgeThreshold);
        return safeWalkMotion.x == 0.0 || safeWalkMotion.y == 0.0;
    }

    public static boolean canSeeEntityMC(PlayerEntity player, Entity entity) {
        return player.canSee(entity);
    }

    public static boolean isInHole(PlayerEntity player) {
        BlockPos pos = player.getBlockPos();
        return player.getWorld().getBlockState(pos.down()).isFullCube(player.getWorld(), pos) && player.getWorld().getBlockState(pos.north()).isFullCube(player.getWorld(), pos) && player.getWorld().getBlockState(pos.south()).isFullCube(player.getWorld(), pos) && player.getWorld().getBlockState(pos.east()).isFullCube(player.getWorld(), pos) && player.getWorld().getBlockState(pos.west()).isFullCube(player.getWorld(), pos);
    }

    public static boolean hasHorizontalCollision(Vec3d pos) {
        return mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(pos.subtract(mc.player.getPos()))).iterator().hasNext();
    }

    public static boolean willFallMoreThanFiveBlocks(Vec3d pos) {
       return willFallXBlocks(pos, 5);
    }
    public static boolean willFallXBlocks(Vec3d pos, int blocks) {
        BlockPos blockPos = BlockPos.Mutable.ofFloored(pos);
        for (int i = 0; i < blocks; i++) {
            blockPos = blockPos.down();
            if (!mc.world.isAir(blockPos)) {
                return false;
            }
        }
        return true;
    }
    public static boolean isSpaceBelowEmpty(double height) {
        Box bb = mc.player.getBoundingBox();
        for (double i = 0; i < height + 0.51; i += 0.01) {
            if (!mc.world.isSpaceEmpty(mc.player, bb.offset(0, -i, 0))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPlayerLookingAtEntity(PlayerEntity player, Entity target, double maxDistance) {
        Vec3d eyePos = player.getEyePos();
        double squaredDist = Math.pow(maxDistance, 2);
        Vec3d rotationVec = player.getRotationVector();
        Vec3d vec3d3 = eyePos.add(rotationVec.multiply(maxDistance));
        Box box = player.getBoundingBox().stretch(rotationVec.multiply(maxDistance)).expand(1.0);
        EntityHitResult entityHitResult = ProjectileUtil.raycast(player, eyePos, vec3d3, box, (entity) -> !entity.isSpectator() && entity.canHit(), squaredDist);
        if (entityHitResult != null) {
            return entityHitResult.getEntity() == target;
        }
        return false;
    }

    public static boolean hasWeaponInHand(PlayerEntity player) {
        if (player.getMainHandStack() == null) return false;
        Item item = player.getMainHandStack().getItem();
        return item instanceof SwordItem || item instanceof AxeItem || item instanceof TridentItem || item instanceof PickaxeItem /* || item instanceof MaceItem*/;
    }

    public static boolean hasRangedWeaponInHand(PlayerEntity player) {
        if (player.getMainHandStack() == null) return false;
        Item item = player.getMainHandStack().getItem();
        return item instanceof BowItem || item instanceof CrossbowItem || item instanceof SnowballItem || item instanceof EggItem || item instanceof TridentItem;
    }
    public static boolean isHolding(final Item item) {
        ItemStack handStack = mc.player.getMainHandStack();
        if (!handStack.isEmpty() && handStack.getItem() == item) return true;
        handStack = mc.player.getOffHandStack();
        return !handStack.isEmpty() && handStack.getItem() == item;
    }

    public static boolean isSprinting(PlayerEntity player) {
        return player.isSprinting();
    }

    public static double getDistanceToEntity(PlayerEntity player, Entity entity) {
        return player.distanceTo(entity);
    }

    public static double getDistanceToBlockPos(PlayerEntity player, BlockPos pos) {
        return Math.sqrt(player.getBlockPos().getSquaredDistance(pos));
    }

    public static double getSquaredDistanceToBP(BlockPos pos) {
        return mc.player.getBlockPos().getSquaredDistance(pos);
    }

    public static double getDistanceToVec3d(PlayerEntity player, Vec3d vec) {
        return Math.sqrt(player.getPos().squaredDistanceTo(vec));
    }

    public static List<Entity> getEntitiesWithinDistance(PlayerEntity player, double distance) {
        Box box = new Box(player.getPos(), player.getPos()).expand(distance);
        return player.getWorld().getOtherEntities(player, box);
    }

    public static List<BlockPos> getBlocksWithinDistance(PlayerEntity player, double distance) {
        List<BlockPos> blocks = new ArrayList<>();
        BlockPos playerPos = player.getBlockPos();
        for (int x = (int) -distance; x <= distance; x++) {
            for (int y = (int) -distance; y <= distance; y++) {
                for (int z = (int) -distance; z <= distance; z++) {
                    blocks.add(playerPos.add(x, y, z));
                }
            }
        }
        return blocks;
    }

    public static List<BlockPos> getBlocksWithinDistance(PlayerEntity player, double distance, double Ydistance) {
        List<BlockPos> blocks = new ArrayList<>();
        BlockPos playerPos = player.getBlockPos();
        for (int x = (int) -distance; x <= distance; x++) {
            for (int z = (int) -distance; z <= distance; z++) {
                for (int y = Math.max(HeliosClient.MC.world.getBottomY(), (int) -Ydistance); y <= Ydistance; y++) {
                    if (y > HeliosClient.MC.world.getTopY()) break;
                    blocks.add(playerPos.add(x, y, z));
                }
            }
        }
        return blocks;
    }


    public static Vec3d getHorizontalVelocity(double bps) {
        if (HeliosClient.MC.player == null) return Vec3d.ZERO;
        bps = bps / 10.0;
        double yawRadians = Math.toRadians(HeliosClient.MC.player.getYaw());
        double forwardInput = HeliosClient.MC.player.input.movementForward;
        double sidewardInput = HeliosClient.MC.player.input.movementSideways;

        // Calculate the velocity components based on input
        double velX = bps * (sidewardInput * Math.cos(yawRadians) - forwardInput * Math.sin(yawRadians));
        double velZ = bps * (sidewardInput * Math.sin(yawRadians) + forwardInput * Math.cos(yawRadians));

        return new Vec3d(velX, 0.0, velZ);
    }

    public static void doLeftClick() {
        HeliosClient.MC.options.attackKey.setPressed(true);
        ((AccessorMinecraftClient) HeliosClient.MC).leftClick();
        HeliosClient.MC.options.attackKey.setPressed(false);
    }

    public static void doRightClick() {
        HeliosClient.MC.options.useKey.setPressed(true);
        ((AccessorMinecraftClient) HeliosClient.MC).rightClick();
        HeliosClient.MC.options.useKey.setPressed(false);
    }

    public static int getDurabilityColor(double durability) {
        if (durability > 0.5) {
            return blend(Color.RED, Color.GREEN, (float) ((durability - 0.5f) * 2)).getRGB();
        } else {
            return blend(Color.RED, Color.YELLOW, (float) (durability * 2)).getRGB();
        }
    }

    public PlayerEntity getPlayer() {
        return HeliosClient.MC.player;
    }
}
