package dev.heliosclient.module;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.PlayerMotionEvent;
import dev.heliosclient.event.events.RenderEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.KeyBind;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.util.ChatUtils;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;

/**
 * Template for modules.
 */
public abstract class Module_ implements Listener {
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    public String name;
    public String description;
    public Category category;
    public ArrayList<Setting> settings;
    public ArrayList<Setting> quickSettings;
    public boolean settingsOpen = false;


    /**
     * Setting indicating if chat feedback for this module should be shown. Don't remove, that will cause crash.
     */
    public BooleanSetting chatFeedback = new BooleanSetting("Enable chat feedback", "Toggles feedback in chat.", this, false);

    /**
     * Setting that will tell module list if it should be shown. Don't remove, that will cause crash.
     */
    public BooleanSetting showInModulesList = new BooleanSetting("Show in Modules List", "If this module should show up in Module List.", this, true);

    /**
     * Key-bind setting. Don't remove, that will cause crash.
     */
    public KeyBind keyBind = new KeyBind("Keybind", "Key to toggle this module.", this, 0);

    /**
     * Value indicating if module is enabled. Don't remove, that will cause crash.
     */
    public BooleanSetting active = new BooleanSetting("Active", "State of this module.", this, false);

    public Module_(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        settings = new ArrayList<>();
        quickSettings = new ArrayList<>();
    }

    /**
     * Called on enable. Probably shouldn't disable original functionality since it will remove chat feedback.
     */
    public void onEnable() {
        if (chatFeedback.value) {
            assert mc.player != null;
            ChatUtils.sendHeliosMsg(this.name + " was enabled.");
        }
        EventManager.register(this);
    }

    /**
     * @return Active status.
     */
    public boolean isActive() {
        return active.value;
    }

    /**
     * Called on disable. Probably shouldn't disable original functionality since it will remove chat feedback.
     */
    public void onDisable() {
        if (chatFeedback.value) {
            assert mc.player != null;
            ChatUtils.sendHeliosMsg(this.name + " was disabled.");
        }
        EventManager.unregister(this);
    }

    /**
     * Called on motion.
     */
    @SubscribeEvent
    public void onMotion(PlayerMotionEvent event) {
    }

    /**
     * Called on tick.
     */
    @SubscribeEvent
    public void onTick(TickEvent event) {
    }

    /**
     * Called on render.
     */
    @SubscribeEvent
    public void render(RenderEvent event) {
    }

    /**
     * Toggles the module.
     */
    public void toggle() {
        active.value = !active.value;
        if (active.value) {
            onEnable();
        } else {
            onDisable();
        }
    }

    /**
     * @return Key that is this module bound to.
     */
    public Integer getKeybind() {
        return keyBind.value;
    }

    /**
     * Sets key-bind to a module.
     *
     * @param keycode Target key-code represented by an GLFW integer.
     */
    public void setKeybind(Integer keycode) {
        keyBind.value = keycode;
    }

    /**
     * Called on load. Override to remove default settings.
     */
    public void onLoad() {
        //Add default settings.
        settings.add(showInModulesList);
        settings.add(chatFeedback);
        settings.add(keyBind);
        settings.add(active);
    }

    /**
     * Called when setting is changed.
     *
     * @param setting Setting that got changed.
     */
    public void onSettingChange(Setting setting) {
        if (setting == active) {
            if (active.value) {
                onEnable();
            } else {
                onDisable();
            }
        }
    }
}
