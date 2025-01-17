package dev.heliosclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.events.render.CrosshairRenderEvent;
import dev.heliosclient.event.events.render.RenderEvent;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.chat.ChatTweaks;
import dev.heliosclient.module.modules.render.CustomCrosshair;
import dev.heliosclient.module.modules.render.NoRender;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profilers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow public abstract void render(DrawContext context, RenderTickCounter tickCounter);

    @Shadow @Final private MinecraftClient client;

    @Inject(at = @At("TAIL"), method = "render", cancellable = true)
    public void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        Profilers.get().push(HeliosClient.MODID + "_render2dEvent");

        Renderer2D.setDrawContext(context);

        RenderEvent event = RenderEvent.get(context, tickCounter.getTickDelta(false));
        EventManager.postEvent(event);
        if (event.isCanceled()) {
            ci.cancel();
        }

        Profilers.get().pop();
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderPortalOverlay(DrawContext context, float nauseaStrength, CallbackInfo ci) {
        if (NoRender.get().noPortal.value && NoRender.get().isActive()) ci.cancel();
    }

    @Inject(method = "renderOverlay", at = @At(value = "HEAD"), cancellable = true)
    private void onRenderOverlay$PumpkinOverlay(DrawContext context, Identifier texture, float opacity, CallbackInfo ci) {
        if (NoRender.get().noPumpkin.value && NoRender.get().isActive() && texture.getPath().equals("textures/misc/pumpkinblur.png")) {
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
    private void onRenderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        //This one is better than the other one.
        if (NoRender.get().noCrosshair.value && NoRender.get().isActive()){
            ci.cancel();
        }
    }
    @ModifyExpressionValue(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;"))
    private Object onRenderCrosshair$RemoveAttackIndicator(Object original) {
        if (ModuleManager.get(CustomCrosshair.class).isActive() && !ModuleManager.get(CustomCrosshair.class).renderAttackIndicator.value){
            return AttackIndicator.OFF;
        }
        return original;
    }
    @Redirect(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V",opcode = 0))
    private void onRenderCrosshair$CustomCrosshair(DrawContext instance, Function<Identifier, RenderLayer> renderLayers, Identifier sprite, int x, int y, int width, int height) {
        //If event is not canceled.
        if(! EventManager.postEvent(new CrosshairRenderEvent(instance,client.getWindow().getScaledWidth()/2 ,client.getWindow().getScaledHeight()/2,width,height)).isCanceled() ){
            instance.drawGuiTexture(renderLayers, sprite, x, y, width, height);
        }
    }

    @Inject(method = "clear", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;clear(Z)V"), cancellable = true)
    private void onClear(CallbackInfo info) {
        if (ModuleManager.get(ChatTweaks.class).keepHistory.value && ModuleManager.get(ChatTweaks.class).isActive()) {
            info.cancel();
        }
    }

}
