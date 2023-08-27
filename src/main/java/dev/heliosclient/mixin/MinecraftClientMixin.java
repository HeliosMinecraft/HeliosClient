package dev.heliosclient.mixin;

import dev.heliosclient.event.EventManager;
import dev.heliosclient.event.events.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import dev.heliosclient.HeliosClient;
import net.minecraft.client.MinecraftClient;

import static net.minecraft.client.MinecraftClient.getInstance;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin
{


	@Inject(method = "tick", at = @At(value = "HEAD"), cancellable = true)
	public void onTick(CallbackInfo ci)
	{
		TickEvent ClientTick = new TickEvent.CLIENT(getInstance());
		EventManager.postEvent(ClientTick);

		TickEvent DefaultTick = new TickEvent();
		EventManager.postEvent(DefaultTick);

		if (ClientTick.isCanceled() || DefaultTick.isCanceled()) {
			ci.cancel();
		}
	}

    @Inject(at = @At("TAIL"), method = "scheduleStop")
	public void onShutdown(CallbackInfo ci) {
		HeliosClient.INSTANCE.saveConfig();
	}
}
