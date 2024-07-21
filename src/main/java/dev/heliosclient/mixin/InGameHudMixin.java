package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.events.render.RenderEvent;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.chat.ChatTweaks;
import dev.heliosclient.module.modules.render.NoRender;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow
    @Final
    private static Identifier PUMPKIN_BLUR;

    @Inject(at = @At("TAIL"), method = "render", cancellable = true)
    public void onRender(DrawContext drawContext, float tickDelta, CallbackInfo info) {
        HeliosClient.MC.getProfiler().push(HeliosClient.MODID + "_render2dEvent");

        Renderer2D.setDrawContext(drawContext);

        RenderEvent event = RenderEvent.get(drawContext, tickDelta);
        EventManager.postEvent(event);
        if (event.isCanceled()) {
            info.cancel();
        }

        HeliosClient.MC.getProfiler().pop();
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderPortalOverlay(DrawContext context, float nauseaStrength, CallbackInfo ci) {
        if (NoRender.get().noPortal.value && NoRender.get().isActive()) ci.cancel();
    }

    @Inject(method = "renderOverlay", at = @At(value = "HEAD"), cancellable = true)
    private void onRenderOverlay$PumpkinOverlay(DrawContext context, Identifier texture, float opacity, CallbackInfo ci) {
        if (NoRender.get().noPumpkin.value && NoRender.get().isActive() && texture == PUMPKIN_BLUR) {
            ci.cancel();
        }
    }

    @Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderVignetteOverlay(DrawContext context, Entity entity, CallbackInfo ci) {
        if (NoRender.get().noVignette.value && NoRender.get().isActive()) ci.cancel();
    }

    @Inject(method = "renderSpyglassOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderSpyglassOverlay(DrawContext context, float scale, CallbackInfo ci) {
        if (NoRender.get().noSpyglass.value && NoRender.get().isActive()) ci.cancel();
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void onRenderCrosshair(DrawContext context, CallbackInfo ci) {
        if (NoRender.get().noCrosshair.value && NoRender.get().isActive()) ci.cancel();
    }

    @Inject(method = "clear", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;clear(Z)V"), cancellable = true)
    private void onClear(CallbackInfo info) {
        if (ModuleManager.get(ChatTweaks.class).keepHistory.value && ModuleManager.get(ChatTweaks.class).isActive()) {
            info.cancel();
        }
    }

}
