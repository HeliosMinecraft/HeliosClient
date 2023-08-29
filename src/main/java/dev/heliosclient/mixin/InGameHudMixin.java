package dev.heliosclient.mixin;

import dev.heliosclient.event.EventManager;
import dev.heliosclient.event.events.RenderEvent;
import dev.heliosclient.module.Module_;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.heliosclient.module.ModuleManager;
import dev.heliosclient.ui.HUDOverlay;
import dev.heliosclient.ui.ModulesListOverlay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;

@Mixin(InGameHud.class)
public class InGameHudMixin {

	@Shadow public int scaledWidth;
    @Shadow public int scaledHeight;
    
	@Inject(at = @At("TAIL"), method = "render") 
	public void onRender (DrawContext drawContext, float tickDelta, CallbackInfo info) 
    {
		if (ModuleManager.INSTANCE.getModuleByName("HUD").active.value) HUDOverlay.INSTANCE.render(drawContext, scaledWidth, scaledHeight);
		if (ModuleManager.INSTANCE.getModuleByName("ModulesList").active.value) ModulesListOverlay.INSTANCE.render(drawContext, scaledWidth, scaledHeight);
		RenderEvent event = new RenderEvent(drawContext,tickDelta);
		EventManager.postEvent(event);
		if (event.isCanceled()){
			info.cancel();
		}
	}

}
