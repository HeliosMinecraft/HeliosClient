package dev.heliosclient.util.world;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.player.RotationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class RaytraceUtils {
    @Nullable
    public static EntityHitResult raycastFromPlayer(float range) {
        Vec3d playerPos = HeliosClient.MC.player.getEyePos();
        float yaw = HeliosClient.MC.player.getYaw();
        float pitch = HeliosClient.MC.player.getPitch();

        Vec3d direction = RotationUtils.getRotationVector(pitch,yaw).normalize();
        Vec3d end = playerPos.add(direction.multiply(range));

        Box box = new Box(playerPos, end);
        Predicate<Entity> predicate = entity -> !entity.isSpectator() && entity.canHit();

        return ProjectileUtil.raycast(HeliosClient.MC.player, playerPos, end, box, predicate, range);
    }
}
