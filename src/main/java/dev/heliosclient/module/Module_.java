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
import dev.heliosclient.module.settings.SettingGroup;
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
    public ArrayList<SettingGroup> settingGroups;
    public ArrayList<SettingGroup> quickSettingGroups;
    public boolean settingsOpen = false;
    public SettingGroup sgbind = new SettingGroup("Bind");

    /**
     * Setting indicating if chat feedback for this module should be shown. Don't remove, that will cause crash.
     */
    public BooleanSetting chatFeedback = sgbind.add(new BooleanSetting.Builder()
            .name("Enable chat feedback")
            .description("Toggles feedback in chat.")
            .module(this)
            .value(false)
            .build()
    );

    /**
     * Setting that will tell module list if it should be shown. Don't remove, that will cause crash.
     */
    public BooleanSetting showInModulesList = sgbind.add(new BooleanSetting.Builder()
            .name("Show in Modules List")
            .description("If this module should show up in Module List.")
            .module(this)
            .value(true)
            .build()
    );
    /**
     * Key-bind setting. Don't remove, that will cause crash.
     */
    public KeyBind keyBind = sgbind.add(new KeyBind.Builder()
            .name("Keybind")
            .description("Key to toggle this module.")
            .module(this)
            .value(0)
            .build()
    );
    /**
     * Value indicating if module is enabled. Don't remove, that will cause crash.
     */
    public BooleanSetting active = sgbind.add(new BooleanSetting.Builder()
            .name("Active")
            .description("State of this module.")
            .module(this)
            .value(false)
            .build()
    );
    public Module_(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        settingGroups = new ArrayList<>();
        quickSettingGroups = new ArrayList<>();
    }

    public void addSettingGroup(SettingGroup settingGroup) {
        this.settingGroups.add(settingGroup);
    }

    public void addQuickSettingGroup(SettingGroup settingGroup) {
        this.quickSettingGroups.add(settingGroup);
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
        addSettingGroup(sgbind);
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
