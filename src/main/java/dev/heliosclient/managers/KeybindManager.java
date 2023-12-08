package dev.heliosclient.managers;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.input.KeyPressedEvent;
import dev.heliosclient.event.events.input.KeyReleasedEvent;
import dev.heliosclient.event.events.input.MouseClickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.module.Module_;
import net.minecraft.client.MinecraftClient;

public class KeybindManager implements Listener {
    public static KeybindManager INSTANCE = new KeybindManager();
    protected static MinecraftClient mc = MinecraftClient.getInstance();

    public KeybindManager(){
        EventManager.register(this);
    }
    @SubscribeEvent(priority = SubscribeEvent.Priority.HIGHEST)
    public void keyPressedEvent(KeyPressedEvent event){
        if (mc.currentScreen != null) return;

        for (Module_ module : ModuleManager.INSTANCE.modules) {
            if (module.getKeybind() != null && module.getKeybind() != -1 && event.getKey() == module.getKeybind()) {
                if (module.toggleOnBindRelease.value) {
                    module.onEnable();
                } else {
                    module.toggle();
                }
            }
        }
    }

    @SubscribeEvent(priority = SubscribeEvent.Priority.HIGHEST)
    public void keyReleasedEvent(KeyReleasedEvent event){
        if (mc.currentScreen != null) return;

        for (Module_ module : ModuleManager.INSTANCE.modules) {
            if (module.getKeybind() != null && module.getKeybind() != -1 && event.getKey() == module.getKeybind()) {
                if (module.toggleOnBindRelease.value) {
                    module.onDisable();
                }
            }
        }
    }
    @SubscribeEvent(priority = SubscribeEvent.Priority.HIGHEST)
    public void mouseClicked(MouseClickEvent event){
        if (mc.currentScreen != null) return;

        for (Module_ module : ModuleManager.INSTANCE.modules) {
            if (module.getKeybind() != null && module.getKeybind() != -1 && event.getButton() == module.getKeybind()) {
                if (module.toggleOnBindRelease.value) {
                    module.onEnable();
                } else {
                    module.toggle();
                }
            }
        }
    }
}
