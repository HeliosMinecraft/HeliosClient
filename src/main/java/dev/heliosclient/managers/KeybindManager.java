package dev.heliosclient.managers;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.input.KeyPressedEvent;
import dev.heliosclient.event.events.input.KeyReleasedEvent;
import dev.heliosclient.event.events.input.MouseClickEvent;
import dev.heliosclient.event.events.input.MouseReleaseEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.module.Module_;
import dev.heliosclient.scripting.LuaFile;
import dev.heliosclient.scripting.LuaScriptManager;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import dev.heliosclient.ui.notification.notifications.InfoNotification;
import dev.heliosclient.util.SoundUtils;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import org.lwjgl.glfw.GLFW;

public class KeybindManager implements Listener {
    private static final int[] CPS = new int[2];
    public static KeybindManager INSTANCE = new KeybindManager();
    static boolean isWPressed = false, isAPressed  = false, isSPressed  = false, isDPressed  = false, isSpacePressed  = false;
    // For KeyStrokes.
    //Todo: Make KeyStrokes element
    private static long lastLeftClick = 0;
    private static long lastRightClick = 0;

    public KeybindManager() {
        EventManager.register(this);
    }

    @SubscribeEvent(priority = SubscribeEvent.Priority.HIGHEST)
    public void keyPressedEvent(KeyPressedEvent event) {
        int key = event.getKey();

        if (key == HeliosClient.CLICKGUI.clickGUIKeyBind.value && !(HeliosClient.MC.currentScreen instanceof ChatScreen) && !(HeliosClient.MC.currentScreen instanceof AbstractInventoryScreen)
                && !(HeliosClient.MC.currentScreen instanceof GameMenuScreen)) {
            ClickGUIScreen.INSTANCE.onLoad();
            HeliosClient.MC.setScreen(ClickGUIScreen.INSTANCE);
        }
        if (key == HeliosClient.CLICKGUI.consoleScreen.value) {
            HeliosClient.MC.setScreen(HeliosClient.CONSOLE);
        }
        if (HeliosClient.MC.currentScreen != null) return;

        for (Module_ module : ModuleManager.INSTANCE.modules) {
            if (module.getKeybind() != null && module.getKeybind() != -1 && key == module.getKeybind()) {
                if (module.toggleOnBindRelease.value) {
                    module.onEnable();
                    module.sendEnableNotification();
                } else {
                    module.toggle();
                }
            }
        }

        for(int i = 0; i < LuaScriptManager.luaFiles.size();i++){
            LuaFile file = LuaScriptManager.luaFiles.get(i);
            if(file.bindKey != -1 && file.bindKey == key) {
                LuaScriptManager.toggleScript(file);
            }
        }

        switch (key) {
            case GLFW.GLFW_KEY_W -> isWPressed = true;
            case GLFW.GLFW_KEY_A -> isAPressed = true;
            case GLFW.GLFW_KEY_S -> isSPressed = true;
            case GLFW.GLFW_KEY_D -> isDPressed = true;
            case GLFW.GLFW_KEY_SPACE -> isSpacePressed = true;
            default -> {
            }
        }
    }

    @SubscribeEvent(priority = SubscribeEvent.Priority.HIGHEST)
    public void keyReleasedEvent(KeyReleasedEvent event) {
        if (HeliosClient.MC.currentScreen != null) return;
        int key = event.getKey();

        for (Module_ module : ModuleManager.INSTANCE.modules) {
            if (module.getKeybind() != null && module.getKeybind() != -1 && key == module.getKeybind()) {
                if (module.toggleOnBindRelease.value) {
                    module.onDisable();
                    module.sendDisableNotification();
                }
            }
        }
        switch (key) {
            case GLFW.GLFW_KEY_W -> isWPressed = false;
            case GLFW.GLFW_KEY_A -> isAPressed = false;
            case GLFW.GLFW_KEY_S -> isSPressed = false;
            case GLFW.GLFW_KEY_D -> isDPressed = false;
            case GLFW.GLFW_KEY_SPACE -> isSpacePressed = false;
            default -> {
            }
        }
    }

    @SubscribeEvent(priority = SubscribeEvent.Priority.HIGHEST)
    public void mouseClicked(MouseClickEvent event) {
        if (HeliosClient.MC.currentScreen != null) return;

        for (Module_ module : ModuleManager.INSTANCE.modules) {
            if (module.getKeybind() != null && module.getKeybind() != -1 && event.getButton() == module.getKeybind()) {
                if (module.toggleOnBindRelease.value) {
                    module.onEnable();
                    module.sendEnableNotification();
                } else {
                    module.toggle();
                }
            }
        }
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            CPS[0]++;
        }
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            CPS[1]++;
        }
    }
    @SubscribeEvent(priority = SubscribeEvent.Priority.HIGHEST)
    public void mouseReleased(MouseReleaseEvent event) {
        if (HeliosClient.MC.currentScreen != null) return;

        for (Module_ module : ModuleManager.INSTANCE.modules) {
            if (module.getKeybind() != null && module.getKeybind() != -1 && event.getButton() == module.getKeybind()) {
                if (module.toggleOnBindRelease.value) {
                    module.onDisable();
                    module.sendDisableNotification();
                }
            }
        }
    }


    @SubscribeEvent(priority = SubscribeEvent.Priority.HIGH)
    public void onTick(TickEvent.WORLD event) {
        // So that only clicks and key presses within the game screen is recorded to prevent any problems.
        if (HeliosClient.MC.currentScreen != null) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLeftClick > 1000) {
            CPS[0] = 0;
            lastLeftClick = currentTime;
        }
        if (currentTime - lastRightClick > 1000) {
            CPS[1] = 0;
            lastRightClick = currentTime;
        }
    }

    public int getLeftCPS() {
        return CPS[0];
    }

    public int getRightCPS() {
        return CPS[1];
    }
}
