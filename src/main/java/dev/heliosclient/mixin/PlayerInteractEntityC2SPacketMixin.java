package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.system.mixininterface.IPlayerInteractEntityC2SPacket;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


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
}