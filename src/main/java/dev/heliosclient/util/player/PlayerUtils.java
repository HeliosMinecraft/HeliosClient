package dev.heliosclient.util.player;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.mixin.AccessorMinecraftClient;
import dev.heliosclient.util.EntityUtils;
import dev.heliosclient.util.MathUtils;
import dev.heliosclient.util.render.Renderer2D;
import dev.heliosclient.util.render.Renderer3D;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;

public class PlayerUtils {
    static MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean canSeeEntity(Entity entity) {
        Vec3d playerEyePos = mc.player.getEyePos();
        Box entityBox = entity.getBoundingBox();
        return mc.player.getWorld().raycast(new RaycastContext(playerEyePos, entityBox.getCenter(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player)).getType() == HitResult.Type.MISS;
    }
    public static boolean isPlayerAtEdge(double edgeThreshold) {
        Vec3d playerPos = Renderer3D.getInterpolatedPosition(mc.player);
        BlockPos blockPos = mc.player.getBlockPos();

        // Get the player's position within the block
        double offsetX = playerPos.x - blockPos.getX();
        double offsetZ = playerPos.z - blockPos.getZ();

        // Check if the player is near the edge of the block
        boolean nearEdgeX = offsetX < edgeThreshold || offsetX > (1 - edgeThreshold);
        boolean nearEdgeZ = offsetZ < edgeThreshold || offsetZ > (1 - edgeThreshold);

        // Check if the block next to the edge is air or something the player can fall through
        if (nearEdgeX || nearEdgeZ) {
            BlockPos edgeBlockPos = blockPos.add(nearEdgeX ? (offsetX < edgeThreshold ? -1 : 1) : 0, 0, nearEdgeZ ? (offsetZ < edgeThreshold ? -1 : 1) : 0);
            BlockState edgeBlockState = mc.world.getBlockState(edgeBlockPos);
            return edgeBlockState.isAir() || edgeBlockState.isReplaceable();
        }

        return false;
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

    public PlayerEntity getPlayer() {
        return HeliosClient.MC.player;
    }
}
