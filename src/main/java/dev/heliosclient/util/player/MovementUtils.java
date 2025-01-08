package dev.heliosclient.util.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

public class MovementUtils {
    static final MinecraftClient mc = MinecraftClient.getInstance();

    public static Vec2f performSafeMovement(double motionX, double motionZ) {
        return performSafeMovement(motionX, motionZ, 0.05);
    }

    public static Vec2f performSafeMovement(double motionX, double motionZ, double adjustment) {
        double adjustedX = motionX;
        double adjustedZ = motionZ;

        float verticalOffset = -mc.player.getStepHeight();
        if (!mc.player.isOnGround()) {
            verticalOffset = -1.5f;
        }

        // Adjust X movement
        while (adjustedX != 0.0 && mc.world.isSpaceEmpty(mc.player, mc.player.getBoundingBox().offset(adjustedX, verticalOffset, 0.0))) {
            if (Math.abs(adjustedX) < adjustment) {
                adjustedX = 0.0;
            } else {
                adjustedX += (adjustedX > 0) ? -adjustment : adjustment;
            }
        }

        // Adjust Z movement
        while (adjustedZ != 0.0 && mc.world.isSpaceEmpty(mc.player, mc.player.getBoundingBox().offset(0.0, verticalOffset, adjustedZ))) {
            if (Math.abs(adjustedZ) < adjustment) {
                adjustedZ = 0.0;
            } else {
                adjustedZ += (adjustedZ > 0) ? -adjustment : adjustment;
            }
        }

        // Adjust both X and Z movements simultaneously
        while (adjustedX != 0.0 && adjustedZ != 0.0 && mc.world.isSpaceEmpty(mc.player, mc.player.getBoundingBox().offset(adjustedX, verticalOffset, adjustedZ))) {
            if (Math.abs(adjustedX) < adjustment) {
                adjustedX = 0.0;
            } else {
                adjustedX += (adjustedX > 0) ? -adjustment : adjustment;
            }

            if (Math.abs(adjustedZ) < adjustment) {
                adjustedZ = 0.0;
            } else {
                adjustedZ += (adjustedZ > 0) ? -adjustment : adjustment;
            }
        }

        return new Vec2f((float) adjustedX, (float) adjustedZ);
    }

    public static boolean isPressingMovementButton() {
        Input input = mc.player.input;
        return input.playerInput.forward() || input.playerInput.backward() || input.playerInput.left() || input.playerInput.right();
    }

    public static boolean isMoving(PlayerEntity player) {
        double d = player.getX() - player.prevX;
        double e = player.getY() - player.prevY;
        double f = player.getZ() - player.prevZ;
        return MathHelper.squaredMagnitude(d, e, f) > MathHelper.square(2.0e-4);
    }

}
