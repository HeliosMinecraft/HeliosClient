package dev.heliosclient.mixin;

import dev.heliosclient.event.EventManager;
import dev.heliosclient.event.events.PlayerMotionEvent;
import dev.heliosclient.module.ModuleManager;
import dev.heliosclient.module.Module_;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin 
{
    @Inject(method = "move", at = @At(value = "TAIL"), cancellable = true)
    public void onMove(MovementType type, Vec3d movement, CallbackInfo ci) 
    {
        for (Module_ m : ModuleManager.INSTANCE.getEnabledModules())
        {
            m.onMotion(type, movement);
        }
        PlayerMotionEvent event = new PlayerMotionEvent(type,movement);
        EventManager.postEvent(event);
        if (event.isCanceled()){
            ci.cancel();
        }
    }

    @Inject(method = "init", at = @At(value = "TAIL"), cancellable = true)
    public void onInit(CallbackInfo ci) 
    {
        for (Module_ m : ModuleManager.INSTANCE.getEnabledModules()) 
        {
			m.onEnable();
		}
    }
}
