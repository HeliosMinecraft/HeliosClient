package dev.heliosclient.mixin;

import dev.heliosclient.event.events.input.MouseClickEvent;
import dev.heliosclient.event.events.input.MouseReleaseEvent;
import dev.heliosclient.event.events.input.MouseScrollEvent;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.render.FreeLook;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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
        //Translate screen mouse positions to minecraft friendly positions
        double mouseX = getX() * (double) this.client.getWindow().getScaledWidth() / (double) this.client.getWindow().getWidth();
        double mouseY = getY() * (double) this.client.getWindow().getScaledHeight() / (double) this.client.getWindow().getHeight();

        if (action == GLFW.GLFW_PRESS) {
            MouseClickEvent event = new MouseClickEvent(window, button, action, mouseX, mouseY, client.currentScreen, mods);
            if (EventManager.postEvent(event).isCanceled()) {
                ci.cancel();
            }
        }
        if (action == GLFW.GLFW_RELEASE) {
            MouseReleaseEvent event = new MouseReleaseEvent(window, button, action, mouseX, mouseY, client.currentScreen, mods);
            if (EventManager.postEvent(event).isCanceled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onMouseScroll", at = @At(value = "TAIL"), cancellable = true)
    private void onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        //Translate screen mouse positions to minecraft friendly positions
        double mouseX = getX() * (double) this.client.getWindow().getScaledWidth() / (double) this.client.getWindow().getWidth();
        double mouseY = getY() * (double) this.client.getWindow().getScaledHeight() / (double) this.client.getWindow().getHeight();

        MouseScrollEvent event = new MouseScrollEvent(mouseX, mouseY, vertical, horizontal);
        if (EventManager.postEvent(event).isCanceled()) {
            ci.cancel();
        }
    }

    @Redirect(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"))
    private void updateChangeLookDirection(ClientPlayerEntity player, double cursorDeltaX, double cursorDeltaY) {
        FreeLook freeLook = ModuleManager.get(FreeLook.class);

        if (freeLook != null && freeLook.isActive()) {
            freeLook.cameraYaw += (float) (cursorDeltaX / freeLook.sensitivity.value);
            freeLook.cameraPitch += (float) (cursorDeltaY / freeLook.sensitivity.value);

            freeLook.cameraPitch = MathHelper.clamp(freeLook.cameraPitch, -90.0F, 90.0F);
        } else player.changeLookDirection(cursorDeltaX, cursorDeltaY);
    }
}
