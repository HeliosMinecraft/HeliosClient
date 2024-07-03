package dev.heliosclient.mixin;

import dev.heliosclient.event.events.input.CharTypedEvent;
import dev.heliosclient.event.events.input.KeyHeldEvent;
import dev.heliosclient.event.events.input.KeyPressedEvent;
import dev.heliosclient.event.events.input.KeyReleasedEvent;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.misc.NoNarrator;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo info) {
        if (key >= 0 && window == MinecraftClient.getInstance().getWindow().getHandle() && InputUtil.isKeyPressed(window, key)) {
            if (action != GLFW.GLFW_RELEASE) {
                KeyHeldEvent event = new KeyHeldEvent(window, key, scancode, action, modifiers);
                EventManager.postEvent(event);
                if (event.isCanceled()) {
                    info.cancel();
                }
            }
            if (action == GLFW.GLFW_PRESS) {
                KeyPressedEvent event = new KeyPressedEvent(window, key, scancode, action, modifiers);
                EventManager.postEvent(event);
                if (event.isCanceled()) {
                    info.cancel();
                }
            }
            if (action == GLFW.GLFW_RELEASE) {
                KeyReleasedEvent event = new KeyReleasedEvent(window, key, scancode, action, modifiers);
                EventManager.postEvent(event);
                if (event.isCanceled()) {
                    info.cancel();
                }
            }
        }
    }


    @Redirect(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getNarratorHotkey()Lnet/minecraft/client/option/SimpleOption;"))
    private SimpleOption<Boolean> disableNarratorKey(GameOptions instance) {
        if (ModuleManager.get(NoNarrator.class).isActive()) {
            instance.getNarratorHotkey().setValue(false);
        }
        return instance.getNarratorHotkey();
    }


    @Inject(method = "onChar", at = @At("HEAD"), cancellable = true)
    private void onChar(long window, int i, int j, CallbackInfo info) {
        CharTypedEvent event = new CharTypedEvent(window, (char) i, j);
        EventManager.postEvent(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }
}
