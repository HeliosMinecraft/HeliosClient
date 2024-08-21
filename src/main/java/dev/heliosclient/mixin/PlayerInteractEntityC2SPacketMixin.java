package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.movement.AutoSneak;
import dev.heliosclient.system.mixininterface.IPlayerInteractEntityC2SPacket;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;


@Mixin(PlayerInteractEntityC2SPacket.class)
public abstract class PlayerInteractEntityC2SPacketMixin implements IPlayerInteractEntityC2SPacket {
    @Shadow
    @Final
    private PlayerInteractEntityC2SPacket.InteractTypeHandler type;

    @Shadow @Final private int entityId;

    @Override
    @SuppressWarnings("all")
    public PlayerInteractEntityC2SPacket.InteractType getType() {
        return type.getType();
    }

    @Override
    public Entity getEntity() {
        return HeliosClient.MC.world.getEntityById(entityId);
    }

    @ModifyVariable(method = "<init>(IZLnet/minecraft/network/packet/c2s/play/PlayerInteractEntityC2SPacket$InteractTypeHandler;)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static boolean modifySneaking(boolean sneaking) {
        return (ModuleManager.get(AutoSneak.class).isActive() && ModuleManager.get(AutoSneak.class).packet.value) || sneaking;
    }
}