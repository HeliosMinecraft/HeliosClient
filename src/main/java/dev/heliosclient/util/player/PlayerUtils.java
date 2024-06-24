package dev.heliosclient.util.player;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.mixin.AccessorMinecraftClient;
import dev.heliosclient.util.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.Hand;
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
    public static boolean canSeeEntityMC(PlayerEntity player, Entity entity) {
       return player.canSee(entity);
    }

    public static boolean isInHole(PlayerEntity player) {
        BlockPos pos = player.getBlockPos();
        return player.getWorld().getBlockState(pos.down()).isFullCube(player.getWorld(), pos) && player.getWorld().getBlockState(pos.north()).isFullCube(player.getWorld(), pos) && player.getWorld().getBlockState(pos.south()).isFullCube(player.getWorld(), pos) && player.getWorld().getBlockState(pos.east()).isFullCube(player.getWorld(), pos) && player.getWorld().getBlockState(pos.west()).isFullCube(player.getWorld(), pos);
    }

    public static boolean isMoving(PlayerEntity player) {
        return MathUtils.length2D(player.getPos()) > 0 && (player.getX() != player.prevX && player.getY() != player.prevY && player.getZ() != player.prevZ);
    }

    public static boolean isPlayerLookingAtEntity(PlayerEntity player, Entity target, double maxDistance) {
        HitResult result = player.raycast(maxDistance, MinecraftClient.getInstance().getTickDelta(), false);
        return result.getType() == HitResult.Type.ENTITY && ((EntityHitResult) result).getEntity() == target;
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
