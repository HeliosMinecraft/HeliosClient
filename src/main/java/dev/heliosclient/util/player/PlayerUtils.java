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
        return player.getVelocity().lengthSquared() > 0;
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
        /**
         * Interacts with an entity.
         *
         * @param entity The entity to interact with.
         * @param hand The hand to use.
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
        public static void doLeftClick(){
            HeliosClient.MC.options.attackKey.setPressed(true);
           ((AccessorMinecraftClient) HeliosClient.MC).leftClick();
            HeliosClient.MC.options.attackKey.setPressed(false);
        }
        public static void doRightClick(){
            HeliosClient.MC.options.useKey.setPressed(true);
            ((AccessorMinecraftClient) HeliosClient.MC).rightClick();
            HeliosClient.MC.options.useKey.setPressed(false);
        }
    public PlayerEntity getPlayer(){
        return HeliosClient.MC.player;
    }
}
