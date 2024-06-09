package dev.heliosclient.system.mixininterface;

import net.minecraft.util.math.Vec3d;

public interface IExplosion {
    void heliosClient$set(Vec3d pos, float power, boolean createFire);
}
