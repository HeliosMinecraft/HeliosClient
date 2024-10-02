package dev.heliosclient.util.player;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.mixin.AccessorMinecraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.*;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static dev.heliosclient.util.ColorUtils.blend;

public class PlayerUtils {
    public static MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean isPressingMovementButton() {
        Input input = mc.player.input;
        return input.pressingForward || input.pressingBack || input.pressingLeft || input.pressingRight;
    }

    public static boolean canSeeEntity(Entity entity) {
        Vec3d playerEyePos = mc.player.getEyePos();
        Box entityBox = entity.getBoundingBox();
        return mc.player.getWorld().raycast(new RaycastContext(playerEyePos, entityBox.getCenter(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player)).getType() == HitResult.Type.MISS;
    }

    public static boolean isPlayerNearEdge(double threshold) {
        BlockPos playerPos = mc.player.getBlockPos();
        Box playerBox = mc.player.getBoundingBox();

        // Check the edges of the block the player is standing on
        BlockPos[] edgePositions = {
                playerPos.north(), playerPos.south(), playerPos.east(), playerPos.west(),
                playerPos.north().up(), playerPos.south().up(), playerPos.east().up(), playerPos.west().up()
        };

        for (BlockPos edgePos : edgePositions) {
            if (isEdgeEmpty(edgePos, playerBox, threshold)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEdgeEmpty(BlockPos edgePos, Box playerBox, double threshold) {
        Box edgeBox = new Box(edgePos);
        return mc.world.isAir(edgePos) && !playerBox.intersects(edgeBox) && isWithinThreshold(playerBox, edgeBox, threshold);
    }

    private static boolean isWithinThreshold(Box playerBox, Box edgeBox, double threshold) {
        return playerBox.minX - edgeBox.maxX <= threshold || edgeBox.minX - playerBox.maxX <= threshold ||
                playerBox.minY - edgeBox.maxY <= threshold || edgeBox.minY - playerBox.maxY <= threshold ||
                playerBox.minZ - edgeBox.maxZ <= threshold || edgeBox.minZ - playerBox.maxZ <= threshold;
    }


    public static boolean canSeeEntityMC(PlayerEntity player, Entity entity) {
        return player.canSee(entity);
    }

    public static boolean isInHole(PlayerEntity player) {
        BlockPos pos = player.getBlockPos();
        return player.getWorld().getBlockState(pos.down()).isFullCube(player.getWorld(), pos) && player.getWorld().getBlockState(pos.north()).isFullCube(player.getWorld(), pos) && player.getWorld().getBlockState(pos.south()).isFullCube(player.getWorld(), pos) && player.getWorld().getBlockState(pos.east()).isFullCube(player.getWorld(), pos) && player.getWorld().getBlockState(pos.west()).isFullCube(player.getWorld(), pos);
    }

    public static boolean isMoving(PlayerEntity player) {
        return (player.getVelocity().lengthSquared() > 0 || player.getVelocity().horizontalLengthSquared() > 0) && (player.getX() != player.prevX && player.getY() != player.prevY && player.getZ() != player.prevZ);
    }

    public static boolean hasHorizontalCollision(Vec3d pos) {
        return mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(pos.subtract(mc.player.getPos()))).iterator().hasNext();
    }

    public static boolean willFallMoreThanFiveBlocks(Vec3d pos) {
        BlockPos blockPos = BlockPos.Mutable.ofFloored(pos);
        for (int i = 0; i < 5; i++) {
            blockPos = blockPos.down();
            if (!mc.world.isAir(blockPos)) {
                return false;
            }
        }
        return true;
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
        return item instanceof SwordItem || item instanceof AxeItem || item instanceof TridentItem  /* || item instanceof MaceItem*/;
    }

    public static boolean hasRangedWeaponInHand(PlayerEntity player) {
        if (player.getMainHandStack() == null) return false;
        Item item = player.getMainHandStack().getItem();
        return item instanceof BowItem || item instanceof CrossbowItem || item instanceof SnowballItem || item instanceof EggItem;
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
