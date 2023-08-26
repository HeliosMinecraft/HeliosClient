package dev.heliosclient.mixin;

import dev.heliosclient.event.EventManager;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.system.KeybindManager;
import dev.heliosclient.module.ModuleManager;
import dev.heliosclient.module.Module_;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import dev.heliosclient.HeliosClient;
import net.minecraft.client.MinecraftClient;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin 
{
	@Shadow @Final public Keyboard keyboard;

	@Inject(method = "tick", at = @At(value = "HEAD"), cancellable = true)
	public void onTick(CallbackInfo ci)
	{
		EventManager.postEvent(new TickEvent());

		KeybindManager.onTick();
	}

    @Inject(at = @At("TAIL"), method = "scheduleStop")
	public void onShutdown(CallbackInfo ci) {
		HeliosClient.INSTANCE.saveConfig();
	}
}
