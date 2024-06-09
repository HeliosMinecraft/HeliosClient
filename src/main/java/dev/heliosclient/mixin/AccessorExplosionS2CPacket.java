package dev.heliosclient.mixin;

import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ExplosionS2CPacket.class)
public interface AccessorExplosionS2CPacket {
    @Mutable
    @Accessor("playerVelocityX")
    float getVelocityX();

    @Mutable
    @Accessor("playerVelocityX")
    void setVelocityX(float velocityX);

    @Mutable
    @Accessor("playerVelocityY")
    float getVelocityY();

    @Mutable
    @Accessor("playerVelocityY")
    void setVelocityY(float velocityY);

    @Mutable
    @Accessor("playerVelocityZ")
    float getVelocityZ();

    @Mutable
    @Accessor("playerVelocityZ")
    void setVelocityZ(float velocityZ);

}
