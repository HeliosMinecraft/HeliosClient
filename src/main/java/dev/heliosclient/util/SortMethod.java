package dev.heliosclient.util;

import dev.heliosclient.util.player.RotationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.Comparator;

import static dev.heliosclient.util.render.Renderer3D.mc;

public enum SortMethod implements Comparator<Entity> {
    LowestDistance((entity0, entity1) -> Float.compare(mc.player.distanceTo(entity0), mc.player.distanceTo(entity1))),
    FarthestDistance((entity0, entity1) -> Float.compare(mc.player.distanceTo(entity1), mc.player.distanceTo(entity0))),
    LowestHealth((entity0, entity1) -> Float.compare(((LivingEntity) entity0).getHealth(), ((LivingEntity) entity1).getHealth())),
    HighestHealth((entity0, entity1) -> Float.compare(((LivingEntity) entity1).getHealth(), ((LivingEntity) entity0).getHealth())),
    LowestAngle(SortMethod::sortAngle);

    private final Comparator<Entity> comparator;

    SortMethod(Comparator<Entity> comparator) {
        this.comparator = comparator;
    }

    private static int sortAngle(Entity e1, Entity e2) {
        boolean e1l = e1 instanceof LivingEntity;
        boolean e2l = e2 instanceof LivingEntity;

        if (!e1l && !e2l) return 0;
        else if (e1l && !e2l) return 1;
        else if (!e1l) return -1;

        double e1yaw = Math.abs(RotationUtils.getYaw(e1.getPos()) - mc.player.getYaw());
        double e2yaw = Math.abs(RotationUtils.getYaw(e2.getPos()) - mc.player.getYaw());

        double e1pitch = Math.abs(RotationUtils.getPitch(e1.getPos()) - mc.player.getPitch());
        double e2pitch = Math.abs(RotationUtils.getPitch(e2.getPos()) - mc.player.getPitch());

        return Double.compare(e1yaw * e1yaw + e1pitch * e1pitch, e2yaw * e2yaw + e2pitch * e2pitch);
    }

    @Override
    public int compare(Entity o1, Entity o2) {
        return comparator.compare(o1, o2);
    }
}
