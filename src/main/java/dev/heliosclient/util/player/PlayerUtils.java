package dev.heliosclient.util.player;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.mixin.AccessorMinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;

public class PlayerUtils {

    public static boolean canSeeEntity(PlayerEntity player, Entity entity) {
        Vec3d playerEyePos = player.getEyePos();
        Box entityBox = entity.getBoundingBox();
        return player.getWorld().raycast(new RaycastContext(playerEyePos, entityBox.getCenter(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player)).getType() == HitResult.Type.MISS;
    }

    public static boolean isInHole(PlayerEntity player) {
        BlockPos pos = player.getBlockPos();
        return player.getWorld().getBlockState(pos.down()).isFullCube(player.getWorld(), pos) && player.getWorld().getBlockState(pos.north()).isFullCube(player.getWorld(), pos) && player.getWorld().getBlockState(pos.south()).isFullCube(player.getWorld(), pos) && player.getWorld().getBlockState(pos.east()).isFullCube(player.getWorld(), pos) && player.getWorld().getBlockState(pos.west()).isFullCube(player.getWorld(), pos);
    }

    public static boolean isMoving(PlayerEntity player) {
        return player.getVelocity().lengthSquared() > 0 && (player.getX() != player.prevX && player.getY() != player.prevY && player.getZ() != player.prevZ);
    }

    public static boolean isPlayerLookingAtEntity(PlayerEntity player, Entity target, float lookRange, int numSegments) {
        Vec3d playerPos = player.getCameraPosVec(HeliosClient.MC.getTickDelta());
        Vec3d lookDir = player.getRotationVec(HeliosClient.MC.getTickDelta());

        // Calculate the step size for each segment
        double stepSize = (HeliosClient.MC.interactionManager.getReachDistance() / numSegments);

        // Perform intersection checks for each segment
        for (int i = 0; i < numSegments; i++) {
            Vec3d segmentStart = playerPos.add(lookDir.multiply(i * stepSize));
            Vec3d segmentEnd = playerPos.add(lookDir.multiply((i + 1) * stepSize));

            // Create a bounding box for the segment
            Box segmentBox = new Box(
                    Math.min(segmentStart.x, segmentEnd.x),
                    Math.min(segmentStart.y, segmentEnd.y),
                    Math.min(segmentStart.z, segmentEnd.z),
                    Math.max(segmentStart.x, segmentEnd.x),
                    Math.max(segmentStart.y, segmentEnd.y),
                    Math.max(segmentStart.z, segmentEnd.z)
            );

            // Check if the segment intersects with the target's bounding box
            if (target.getBoundingBox().expand(lookRange, 0, lookRange).intersects(segmentBox)) {
                return true;
            }
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


    /**
     * Interacts with an entity.
     *
     * @param entity The entity to interact with.
     * @param hand   The hand to use.
     */
    public static void interactEntity(Entity entity, Hand hand) {
        ClientPlayerInteractionManager interactionManager = HeliosClient.MC.interactionManager;
        if (interactionManager != null) {
            interactionManager.interactEntity(HeliosClient.MC.player, entity, hand);
        }
    }

    /**
     * Attacks an entity.
     *
     * @param entity The entity to attack.
     */
    public static void attackEntity(Entity entity) {
        ClientPlayerInteractionManager interactionManager = HeliosClient.MC.interactionManager;
        if (interactionManager != null) {
            interactionManager.attackEntity(HeliosClient.MC.player, entity);
        }
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
