package dev.heliosclient.mixin;

import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerInputC2SPacket.class)
public interface AccessorPlayerInputC2SPacket {

}
