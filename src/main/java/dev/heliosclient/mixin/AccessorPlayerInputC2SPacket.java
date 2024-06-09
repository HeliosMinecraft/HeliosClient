package dev.heliosclient.mixin;

import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerInputC2SPacket.class)
public interface AccessorPlayerInputC2SPacket {
    @Accessor(value = "jumping")
    void setJumping(boolean jumping);
}
