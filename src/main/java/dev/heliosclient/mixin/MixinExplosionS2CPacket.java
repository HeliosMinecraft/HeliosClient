package dev.heliosclient.mixin;

import dev.heliosclient.system.mixininterface.IExplosionS2CPacket;
import dev.heliosclient.system.mixininterface.IVec3d;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(ExplosionS2CPacket.class)
public abstract class MixinExplosionS2CPacket implements IExplosionS2CPacket {
    @Mutable
    @Shadow @Final private Optional<Vec3d> playerKnockback;

    @Override
    public void helios$setVelocityX(float vX) {
        if(playerKnockback.isPresent()) {
            Vec3d knockBack = playerKnockback.get();
            ((IVec3d)knockBack).heliosClient$setX(vX);
        } else {
            playerKnockback = Optional.of(new Vec3d(vX,0,0));
        }
    }

    @Override
    public void helios$setVelocityY(float vY) {
        if(playerKnockback.isPresent()) {
            Vec3d knockBack = playerKnockback.get();
            ((IVec3d)knockBack).heliosClient$setY(vY);
        } else {
            playerKnockback = Optional.of(new Vec3d(0,vY,0));
        }
    }

    @Override
    public void helios$setVelocityZ(float vZ) {
        if(playerKnockback.isPresent()) {
            Vec3d knockBack = playerKnockback.get();
            ((IVec3d)knockBack).heliosClient$setX(vZ);
        } else {
            playerKnockback = Optional.of(new Vec3d(0,0,vZ));
        }
    }

    @Override
    public double helios$getVelocityX() {
        return playerKnockback.map(vec3d -> vec3d.x).orElse(0.0);
    }

    @Override
    public double helios$getVelocityY() {
        return playerKnockback.map(vec3d -> vec3d.y).orElse(0.0);
    }

    @Override
    public double helios$getVelocityZ() {
        return playerKnockback.map(vec3d -> vec3d.z).orElse(0.0);
    }
}
