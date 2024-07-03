package dev.heliosclient.util.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.util.animation.Easing;
import dev.heliosclient.util.animation.EasingType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public class RotationSimulator implements Listener {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public static boolean pauseInGUI = true;
    // The instance is to be used across modules who want their rotations to be synchronised.
    // It is registered to the event manager by default in HeliosClient.java
    public static RotationSimulator INSTANCE = new RotationSimulator();
    private Rotation currentRotation = null;

    public void simulateRotation(double yaw, double pitch, boolean clientSide, Runnable task, int tickTiming, int randomness, EasingType easingType) {
        if (currentRotation == null) {
            currentRotation = new Rotation(yaw, pitch, clientSide, task, tickTiming, randomness, easingType);
        } else {
            currentRotation.setTarget(yaw, pitch, clientSide, task, tickTiming, randomness, easingType);
        }
    }

    public void simulateRotation(double yaw, double pitch, boolean clientSide, Runnable task, int tickTiming, int randomness) {
        simulateRotation(yaw, pitch, clientSide, task, tickTiming, randomness, EasingType.LINEAR_IN);
    }

    public void simulateRotation(Entity entity, boolean clientSide, Runnable task, int tickTiming, int randomness, RotationUtils.LookAtPos lookAtPos, EasingType type) {
        simulateRotation(RotationUtils.getYaw(lookAtPos.getPosition(entity)), RotationUtils.getPitch(lookAtPos.getPosition(entity)), clientSide, task, tickTiming, randomness, type);
    }

    public void simulateRotation(Entity entity, boolean clientSide, Runnable task, int tickTiming, int randomness, RotationUtils.LookAtPos lookAtPos) {
        simulateRotation(RotationUtils.getYaw(lookAtPos.getPosition(entity)), RotationUtils.getPitch(lookAtPos.getPosition(entity)), clientSide, task, tickTiming, randomness, EasingType.LINEAR_IN);
    }


    @SubscribeEvent
    public void tick(TickEvent.PLAYER event) {
        if (currentRotation != null) {
            currentRotation.update();

            if (currentRotation.isCompleted()) {
                clearRotations();
            }
        }
    }

    public void clearRotations() {
        if (currentRotation != null)
            Rotation.ticksPassed = currentRotation.tickTiming;

        currentRotation = null;
    }

    public int getTicksPassed() {
        if (currentRotation != null) {
            return Rotation.ticksPassed;
        }
        return -1;
    }

    private static class Rotation {
        private static int ticksPassed;
        private final double startYaw;
        private final double startPitch;
        private final Random rand = Random.create(System.currentTimeMillis());
        private double targetYaw;
        private double targetPitch;
        private boolean clientSide;
        private Runnable task;
        private int tickTiming;
        private int randomness;
        private EasingType type;

        public Rotation(double yaw, double pitch, boolean clientSide, Runnable task, int tickTiming, int randomness, EasingType type) {
            this.setTarget(yaw, pitch, clientSide, task, tickTiming, randomness, type);
            ticksPassed = 0;
            this.startYaw = mc.player.getYaw(mc.getTickDelta());
            this.startPitch = mc.player.getPitch(mc.getTickDelta());
        }

        public void setTarget(double yaw, double pitch, boolean clientSide, Runnable task, int tickTiming, int randomness, EasingType type) {
            this.targetYaw = yaw;
            this.targetPitch = pitch;
            this.clientSide = clientSide;
            this.task = task;
            this.tickTiming = tickTiming;
            this.type = type;
            this.randomness = randomness;
        }

        public boolean isCompleted() {
            return (float) ticksPassed / tickTiming >= 1.0f;
        }

        public void update() {
            if (pauseInGUI && mc.currentScreen != null)
                return;

            ticksPassed++;

            double progress = MathHelper.clamp((float) ticksPassed / tickTiming, 0.0f, 1.0f);
            progress = Easing.ease(type, (float) progress);

            if (progress >= 1.0) {
                progress = 1.0;
                if (task != null) {
                    task.run();
                }
            }


            double interpolatedYaw = MathHelper.lerpAngleDegrees((float) progress, startYaw, targetYaw);
            double interpolatedPitch = MathHelper.lerpAngleDegrees((float) progress, startPitch, targetPitch);

            // Add randomness to the target yaw and pitch
            if (randomness != 0.0 && progress < 0.97f && rand.nextBetween(0, 20) > 18) {
                interpolatedYaw += (rand.nextGaussian() - 1) * randomness;
                interpolatedPitch += (rand.nextGaussian() - 1) * randomness;
            }

            RotationUtils.rotate((float) interpolatedYaw, (float) interpolatedPitch, clientSide, null);
            mc.player.renderYaw = mc.player.getYaw(mc.getTickDelta());
            mc.player.renderPitch = mc.player.getPitch(mc.getTickDelta());
        }
    }

}
