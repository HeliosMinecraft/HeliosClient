package dev.heliosclient.mixin;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityVelocityUpdateS2CPacket.class)
public interface AccessorEntityVelocityUpS2CPacket {

    @Accessor("velocityX")
    void setVelocityX(int velocityX);

    @Accessor("velocityY")
    void setVelocityY(int velocityY);

    @Accessor("velocityZ")
    void setVelocityZ(int velocityZ);
}
