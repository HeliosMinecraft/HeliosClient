package dev.heliosclient.util.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.listener.Listener;
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

    public void simulateRotation(double yaw, double pitch, boolean clientSide, Runnable task, int tickTiming, int randomness) {
        if (currentRotation == null) {
            currentRotation = new Rotation(yaw, pitch, clientSide, task, tickTiming,randomness);
        } else {
            currentRotation.setTarget(yaw, pitch, clientSide, task, tickTiming,randomness);
        }
    }

    public void simulateRotation(Entity entity, boolean clientSide, Runnable task, int tickTiming, int randomness, RotationUtils.LookAtPos lookAtPos) {
        simulateRotation(RotationUtils.getYaw(lookAtPos.getPosition(entity)), RotationUtils.getPitch(lookAtPos.getPosition(entity)), clientSide, task, tickTiming,randomness);
    }

    @SubscribeEvent(priority = SubscribeEvent.Priority.HIGHEST)
    public void tick(TickEvent.PLAYER event) {
        if (currentRotation != null) {
            currentRotation.update();
            if (currentRotation.isCompleted()) {
                currentRotation = null;
            }
        }
    }

    public void clearRotations() {
        currentRotation = null;
    }

    private static class Rotation {
        private double targetYaw;
        private double targetPitch;
        private boolean clientSide;
        private Runnable task;
        private int tickTiming;
        private int ticksPassed;
        private int randomness;
        private final Random rand = Random.create(System.currentTimeMillis());

        public Rotation(double yaw, double pitch, boolean clientSide, Runnable task, int tickTiming, int randomness) {
            this.setTarget(yaw, pitch, clientSide, task, tickTiming,randomness);
            ticksPassed = 0;
        }

        public void setTarget(double yaw, double pitch, boolean clientSide, Runnable task, int tickTiming, int randomness) {
            this.targetYaw = yaw;
            this.targetPitch = pitch;
            this.clientSide = clientSide;
            this.task = task;
            this.tickTiming = tickTiming;
            ticksPassed = 0;
            this.randomness = randomness;
        }

        public boolean isCompleted() {
            return (float) ticksPassed / tickTiming >= 1.0f;
        }

        public void update() {
            if (pauseInGUI && mc.currentScreen != null)
                return;

            //Impossible!!!
            if(mc.player == null) return;

            ticksPassed++;

            double progress = MathHelper.clamp((float) ticksPassed / tickTiming, 0.0f, 1.0f);
            if (progress >= 1.0) {
                progress = 1.0;
                if (task != null) {
                    task.run();
                }
            }

            double interpolatedYaw = MathHelper.lerpAngleDegrees((float) progress, mc.player.getYaw(mc.getTickDelta()), targetYaw );
            double interpolatedPitch = MathHelper.lerpAngleDegrees((float) progress, mc.player.getPitch(mc.getTickDelta()), targetPitch );

            // Add randomness to the target yaw and pitch
            if(randomness != 0.0 && rand.nextBetween(0,20) > 18) {
                interpolatedYaw += (rand.nextDouble() - 1) * randomness;
                interpolatedPitch += (rand.nextDouble() - 1) * randomness;
            }

            RotationUtils.rotate((float) interpolatedYaw, (float) interpolatedPitch, clientSide, null);
            mc.player.renderYaw = mc.player.getYaw(mc.getTickDelta());
            mc.player.renderPitch = mc.player.getPitch(mc.getTickDelta());
        }
    }

}
