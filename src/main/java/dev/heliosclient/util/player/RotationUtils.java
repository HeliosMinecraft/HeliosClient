package dev.heliosclient.util.player;

import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.timer.TickTimer;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import static dev.heliosclient.util.render.Renderer3D.mc;

public class RotationUtils {
    //Incremented in rotation simulator
    public static final TickTimer timerSinceLastRotation = new TickTimer(true);
    static float prevYaw, prevPitch;
    public static float serverYaw, serverPitch;

    public static void lookAt(Entity entity, LookAtPos lookAtPos) {
        lookAt(lookAtPos.positionGetter.getPosition(entity));
    }
    public static void lookAt(Entity entity) {
        lookAt(LookAtPos.CENTER.positionGetter.getPosition(entity));
    }

    public static void lookAtEntityMC(Entity entity, LookAtPos lookAtPos) {
        Vec3d vec3d = lookAtPos.positionGetter.getPosition(mc.player);
        double d = entity.getPos().x - vec3d.x;
        double e = entity.getPos().y - vec3d.y;
        double f = entity.getPos().z - vec3d.z;
        double g = Math.sqrt(d * d + f * f);
        mc.player.setPitch(MathHelper.wrapDegrees((float)(-(MathHelper.atan2(e, g) * 57.2957763671875))));
        mc.player.setYaw(MathHelper.wrapDegrees((float)(MathHelper.atan2(f, d) * 57.2957763671875) - 90.0F));
        mc.player.setHeadYaw(mc.player.getYaw());
        mc.player.prevPitch = mc.player.getPitch();
        mc.player.prevYaw = mc.player.getYaw();
        mc.player.prevHeadYaw =  mc.player.headYaw;
        mc.player.bodyYaw =  mc.player.headYaw;
        mc.player.prevBodyYaw =  mc.player.bodyYaw;
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


    public static void rotate(double yaw, double pitch, boolean clientSide, @Nullable Runnable task) {
        if(prevYaw != yaw && prevPitch != pitch && clientSide){
            timerSinceLastRotation.restartTimer();
        }
        prevYaw = mc.player.getYaw(mc.getTickDelta());
        prevPitch = mc.player.getPitch(mc.getTickDelta());

        mc.player.setPitch((float) pitch);
        mc.player.setYaw((float) yaw);

        if (clientSide) {
            mc.player.renderYaw = (float) yaw;
            mc.player.renderPitch = (float) pitch;
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround((float) yaw, (float) pitch, mc.player.isOnGround()));
            if (task != null)
                task.run();
            setServerRotations((float) yaw, (float) pitch);
            mc.player.setYaw(prevYaw);
            mc.player.setPitch(prevPitch);
        }
    }

    public static void setServerRotations(float yaw, float pitch){
        serverYaw = yaw;
        serverPitch = pitch;
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
