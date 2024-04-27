package dev.heliosclient.util.player;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import static dev.heliosclient.util.render.Renderer3D.mc;

public class RotationUtils {
    public static void lookAt(Entity entity) {
        lookAt(entity.getX(),entity.getY(),entity.getZ());
    }
    public static void lookAt(Vec3d pos) {
        lookAt(pos.getX(),pos.getY(),pos.getZ());
    }

    public static void lookAt(double targetX, double targetY, double targetZ) {
        double dx = targetX - mc.player.getX();
        double dy = targetY - mc.player.getEyeY(); // account for the player's eye height
        double dz = targetZ - mc.player.getZ();

        double distanceXZ = Math.sqrt(dx*dx + dz*dz);

        double yaw = Math.toDegrees(Math.atan2(dz, dx)) - 90;
        double pitch = -Math.toDegrees(Math.atan2(dy, distanceXZ));

        mc.player.setYaw((float)yaw);
        mc.player.setPitch((float)pitch);
    }
}
