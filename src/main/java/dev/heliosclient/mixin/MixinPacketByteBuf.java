package dev.heliosclient.mixin;

import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.world.AntiBookBan;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PacketByteBuf.class)
public class MixinPacketByteBuf {

    @ModifyArg(method = "readNbt()Lnet/minecraft/nbt/NbtCompound;", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readNbt(Lio/netty/buffer/ByteBuf;)Lnet/minecraft/nbt/NbtCompound;"))
    private ByteBuf increaseMaxBytes(ByteBuf buf) {
        return ModuleManager.get(AntiBookBan.class).isActive() ? Unpooled.buffer(Integer.MAX_VALUE) : buf;
    }
}