package dev.heliosclient.util.player;

import dev.heliosclient.util.render.Renderer3D;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import static dev.heliosclient.util.render.Renderer3D.mc;

public class RotationUtils {
    static float prevYaw, prevPitch;

    public static void lookAt(Entity entity, LookAtPos lookAtPos) {
        lookAt(lookAtPos.positionGetter.getPosition(entity));
    }

    public static void lookAt(Vec3d pos) {
        lookAt(pos.getX(), pos.getY(), pos.getZ());
    }

    public static void lookAt(double targetX, double targetY, double targetZ) {
        rotate((float) getYaw(targetX, targetZ), (float) getPitch(targetX, targetY, targetZ), false, null);
    }

    public static void lookAt(HitResult result) {
        lookAt(result.getPos());
    }

    public static double getYaw(double targetX, double targetZ) {
        double dx = targetX - mc.player.getX();
        double dz = targetZ - mc.player.getZ();

        return Math.toDegrees(Math.atan2(dz, dx)) - 90;
    }

    public static double getYaw(Vec3d target) {
        double dx = target.getX() - mc.player.getX();
        double dz = target.getZ() - mc.player.getZ();

        return Math.toDegrees(Math.atan2(dz, dx)) - 90;
    }

    public static double getYaw(BlockPos target) {
        double dx = target.getX() - mc.player.getX();
        double dz = target.getZ() - mc.player.getZ();

        return Math.toDegrees(Math.atan2(dz, dx)) - 90;
    }

    public static double getPitch(double targetX, double targetY, double targetZ) {
        double dx = targetX - mc.player.getX();
        double dy = targetY - mc.player.getEyeY(); // account for the player's eye height
        double dz = targetZ - mc.player.getZ();

        double distanceXZ = Math.sqrt(dx * dx + dz * dz);

        return -Math.toDegrees(Math.atan2(dy, distanceXZ));
    }

    public static double getPitch(BlockPos target) {
        double dx = target.getX() - mc.player.getX();
        double dy = target.getY() - mc.player.getEyeY(); // account for the player's eye height
        double dz = target.getZ() - mc.player.getZ();

        double distanceXZ = Math.sqrt(dx * dx + dz * dz);

        return -Math.toDegrees(Math.atan2(dy, distanceXZ));
    }

    public static double getPitch(Vec3d target) {
        double dx = target.getX() - mc.player.getX();
        double dy = target.getY() - mc.player.getEyeY(); // account for the player's eye height
        double dz = target.getZ() - mc.player.getZ();

        double distanceXZ = Math.sqrt(dx * dx + dz * dz);

        return -Math.toDegrees(Math.atan2(dy, distanceXZ));
    }


    public static void rotate(float yaw, float pitch, boolean clientSide, @Nullable Runnable task) {
        prevYaw = mc.player.getYaw(mc.getTickDelta());
        prevPitch = mc.player.getPitch(mc.getTickDelta());

        mc.player.setPitch(pitch);
        mc.player.setYaw(yaw);

        if (clientSide) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround()));
            if (task != null)
                task.run();
            mc.player.setYaw(prevYaw);
            mc.player.setPitch(prevPitch);
        }
    }

    public enum LookAtPos {
        HEAD(Entity::getEyePos),
        CENTER(entity -> entity.getBoundingBox().getCenter()),
        FEET(Renderer3D::getInterpolatedPosition);

        private final PositionGetter positionGetter;

        LookAtPos(PositionGetter positionGetter) {
            this.positionGetter = positionGetter;
        }

        public Vec3d getPosition(Entity entity) {
            return positionGetter.getPosition(entity);
        }

        // Functional interface to get the position
        @FunctionalInterface
        private interface PositionGetter {
            Vec3d getPosition(Entity entity);
        }
    }
}
