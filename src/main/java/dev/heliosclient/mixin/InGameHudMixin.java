package dev.heliosclient.mixin;

import dev.heliosclient.event.EventManager;
import dev.heliosclient.event.events.RenderEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(at = @At("TAIL"), method = "render")
    public void onRender(DrawContext drawContext, float tickDelta, CallbackInfo info) {
        RenderEvent event = new RenderEvent(drawContext, tickDelta);
        EventManager.postEvent(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }

}
