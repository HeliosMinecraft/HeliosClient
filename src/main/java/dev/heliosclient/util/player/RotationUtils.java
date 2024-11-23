package dev.heliosclient.util.player;

import dev.heliosclient.event.events.player.SendMovementPacketEvent;
import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.timer.TickTimer;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Queue;

import static dev.heliosclient.util.render.Renderer3D.mc;

public class RotationUtils {
    // Incremented in rotation simulator
    public static final TickTimer timerSinceLastRotation = new TickTimer(true);
    static float prevYaw, prevPitch;
    public static float serverYaw, serverPitch;

    // Queue to store rotations
    private static final Queue<Rotation> rotationQueue = new LinkedList<>();

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
        mc.player.prevHeadYaw = mc.player.headYaw;
        mc.player.bodyYaw = mc.player.headYaw;
        mc.player.prevBodyYaw = mc.player.bodyYaw;
    }

    /**
     * From minecraft's Entity class
     */
    public static Vec3d getRotationVector(float pitch, float yaw){
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }

    /* == Instantly looks at a given pos == */
    public static void instaLookAt(Vec3d pos) {
        instaLookAt(pos.getX(), pos.getY(), pos.getZ());
    }
    public static void instaLookAt(Entity entity, LookAtPos lookAtPos) {
        instaLookAt(lookAtPos.positionGetter.getPosition(entity));
    }
    public static void instaLookAt(Entity entity) {
        instaLookAt(LookAtPos.CENTER.positionGetter.getPosition(entity));
    }

    public static void instaLookAt(double targetX, double targetY, double targetZ) {
        float yaw = (float) getYaw(targetX, targetZ);
        float pitch = (float) getPitch(targetX,targetY, targetZ);
        setPlayerRotations(yaw,pitch);
    }

    /* == Schedules look at a given pos == */

    public static void lookAt(Vec3d pos) {
        lookAt(pos.getX(), pos.getY(), pos.getZ());
    }
    public static void lookAt(Entity entity, LookAtPos lookAtPos) {
        lookAt(lookAtPos.positionGetter.getPosition(entity));
    }
    public static void lookAt(Entity entity) {
        lookAt(LookAtPos.CENTER.positionGetter.getPosition(entity));
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
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90F;

        return MathHelper.wrapDegrees(yaw);
    }

    public static double getYaw(Vec3d target) {
        return getYaw(target.getX(), target.getZ());
    }

    public static double getYaw(BlockPos target) {
        return getYaw(target.getX(), target.getZ());
    }

    public static double getPitch(double targetX, double targetY, double targetZ) {
        double dx = targetX - mc.player.getX();
        double dy = targetY - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double dz = targetZ - mc.player.getZ();

        double distanceXZ = Math.sqrt(dx * dx + dz * dz);

        float pitch = (float) -Math.toDegrees(Math.atan2(dy, distanceXZ));

        return MathHelper.wrapDegrees(pitch);
    }

    public static double getPitch(BlockPos target) {
        return getPitch(target.getX(), target.getY(), target.getZ());
    }

    public static double getPitch(Vec3d target) {
        return getPitch(target.getX(), target.getY(), target.getZ());
    }

    public static void rotate(double yaw, double pitch, boolean clientSide, @Nullable Runnable task) {
        // Add rotation to the queue
        rotationQueue.add(new Rotation((float) yaw, (float) pitch, clientSide, task));
    }

    public static void setServerRotations(float yaw, float pitch) {
        serverYaw = yaw;
        serverPitch = pitch;
        timerSinceLastRotation.restartTimer();
    }

    private static void setPlayerRotations(float yaw, float pitch){
        prevPitch = mc.player.getPitch(mc.getTickDelta());
        prevYaw = mc.player.getYaw(mc.getTickDelta());

        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }
    private static void resetPlayerRotation(){
        mc.player.setYaw(prevYaw);
        mc.player.setPitch(prevPitch);
    }

    //Called in RotationSimulator
    public static void onPreSendMovementPacket(SendMovementPacketEvent.PRE event) {
        if (mc.cameraEntity != mc.player) return;

        // Process the first rotation in the queue
        if (!rotationQueue.isEmpty()) {
            Rotation rotation = rotationQueue.peek();

            if(rotation != null) {
                setServerRotations(rotation.yaw, rotation.pitch);
                setPlayerRotations(rotation.yaw, rotation.pitch);
            }
        }
    }

    //Called in RotationSimulator
    public static void onPostSendMovementPacket(SendMovementPacketEvent.POST event) {
        // Process the first rotation in the queue
        if (!rotationQueue.isEmpty()) {
            if (mc.cameraEntity == mc.player) {
                rotationQueue.peek().run();

                resetPlayerRotation();
            }

            int i = 0;
            while (!rotationQueue.isEmpty()) {
                Rotation rotation = rotationQueue.poll();
                if (rotation != null) {
                    setServerRotations(rotation.yaw, rotation.pitch);
                    // If clientSide, change client rotation and send PositionAndLook packet
                    if (rotation.clientSide)
                        setPlayerRotations(rotation.yaw, rotation.pitch);

                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(rotation.yaw, rotation.pitch, mc.player.isOnGround()));
                    rotation.run();

                    if (rotation.clientSide)
                        resetPlayerRotation();
                    i++;
                }
            }
            if(i > 0) {
                resetPlayerRotation();
            }
        }
    }

    // Static class to store rotation data
    private static class Rotation {
        float yaw, pitch;
        boolean clientSide;
        @Nullable Runnable task;

        Rotation(float yaw, float pitch, boolean clientSide, @Nullable Runnable task) {
            this.yaw = yaw;
            this.pitch = pitch;
            this.clientSide = clientSide;
            this.task = task;
        }
        public void run(){
            if(task != null){
                task.run();
            }
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
