package dev.heliosclient.managers;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.input.KeyPressedEvent;
import dev.heliosclient.event.events.input.KeyReleasedEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.module.Module_;
import net.minecraft.client.MinecraftClient;

public class KeybindManager implements Listener {
    public static KeybindManager INSTANCE = new KeybindManager();
    protected static MinecraftClient mc = MinecraftClient.getInstance();

  /*  @SubscribeEvent
    public static void onTick(TickEvent event) {
        if (mc == null || mc.player == null) return; // TO BE CHANGED
        if (mc.currentScreen != null) return;
        ArrayList<Integer> isPressed = new ArrayList<>();
        for (Module_ module : ModuleManager.INSTANCE.modules) {
            Integer key = module.getKeybind();
            if (key == 0) {
                continue;
            }
            boolean isKeyPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), key);
            if (isPressed.contains(key)) {
                module.toggle();
                continue;
            }
            if (isKeyPressed) {
                if (!wasPressed.contains(key)) {
                    isPressed.add(key);
                    module.toggle();
                    wasPressed.add(key);
                }

            } else {
                wasPressed.remove(key);
            }
        }
    }

   */
    public KeybindManager(){
        EventManager.register(this);
    }
    @SubscribeEvent
    public void keyPressedEvent(KeyPressedEvent event){
        if (mc.currentScreen != null) return;

        for (Module_ module : ModuleManager.INSTANCE.modules) {
            if (module.getKeybind() != null && module.getKeybind() != 0 && event.getKey() == module.getKeybind()) {
                if (module.toggleOnBindRelease.value) {
                    module.onEnable();
                } else {
                    module.toggle();
                }
            }
        }
    }

    @SubscribeEvent
    public void keyReleasedEvent(KeyReleasedEvent event){
        if (mc.currentScreen != null) return;

        for (Module_ module : ModuleManager.INSTANCE.modules) {
            if (module.getKeybind() != null && module.getKeybind() != 0 && event.getKey() == module.getKeybind()) {
                if (module.toggleOnBindRelease.value) {
                    module.onDisable();
                }
            }
        }
    }
}
