package dev.heliosclient.mixin;

import net.minecraft.entity.player.PlayerPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerPosition.class)
public interface AccessorPlayerPosition {
    @Accessor("pitch")
    void setPitch(float pitch);

    @Accessor("yaw")
    void setYaw(float yaw);
}
