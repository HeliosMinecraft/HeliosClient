package dev.heliosclient.mixin;

import dev.heliosclient.event.events.input.MouseClickEvent;
import dev.heliosclient.managers.EventManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow
    @Final
    private MinecraftClient client;


    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getY();

    @Inject(method = "onMouseButton", at = @At(value = "TAIL"), cancellable = true)
    private void onButton(long window, int button, int action, int mods, CallbackInfo ci) {
        double mouseX = getX() * (double) this.client.getWindow().getScaledWidth() / (double) this.client.getWindow().getWidth();
        double mouseY = getY() * (double) this.client.getWindow().getScaledHeight() / (double) this.client.getWindow().getHeight();

        if (action == GLFW.GLFW_PRESS) {
            MouseClickEvent event = new MouseClickEvent(window, button, action, mouseX, mouseY, client.currentScreen, mods);
            EventManager.postEvent(event);
            if (event.isCanceled()) {
                ci.cancel();
            }
        }
    }

}
