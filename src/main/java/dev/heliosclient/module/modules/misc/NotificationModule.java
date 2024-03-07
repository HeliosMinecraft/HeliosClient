package dev.heliosclient.module.modules.misc;

import dev.heliosclient.managers.NotificationManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.render.GUI;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.ui.notification.Notification;
import dev.heliosclient.util.render.Renderer2D;

import java.util.Collections;
import java.util.List;

public class NotificationModule extends Module_ {
    private final SettingGroup sgNotifications = new SettingGroup("Notifications");
    private final SettingGroup sgConfig = new SettingGroup("Config");
    public BooleanSetting moduleNotification = sgNotifications.add(new BooleanSetting.Builder()
            .name("Module Info")
            .description("Whether to show module state.")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .build()
    );
    public BooleanSetting scriptNotifications = sgNotifications.add(new BooleanSetting.Builder()
            .name("Script Info")
            .description("Whether to show info about script state")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .build()
    );
    public BooleanSetting clientNotification = sgNotifications.add(new BooleanSetting.Builder()
            .name("Client Info")
            .description("Whether to show any messages or info of the client as a notification.")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .build()
    );
    public CycleSetting animationStyle = sgConfig.add(new CycleSetting.Builder()
            .name("Animation Style")
            .description("Type of animation to be applied to notifications")
            .onSettingChange(this)
            .value(List.of(Notification.AnimationStyle.values()))
            .defaultValue(List.of(Notification.AnimationStyle.values()))
            .defaultListIndex(0)
            .build()
    );
    public DoubleSetting maxNotifications = sgConfig.add(new DoubleSetting.Builder()
            .name("Max Notifications Queue")
            .description("The max no. of notifications to be displayed at a time")
            .onSettingChange(this)
            .value(5D)
            .defaultValue(5D)
            .min(1D)
            .max(20D)
            .roundingPlace(0)
            .build()
    );
    public BooleanSetting playSound = sgConfig.add(new BooleanSetting.Builder()
            .name("Play Sound")
            .description("Whether to play a sound when a notification is made")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .build()
    );
    public DoubleSetting volume = sgConfig.add(new DoubleSetting.Builder()
            .name("Notification Volume")
            .description("Volume of notification sound")
            .onSettingChange(this)
            .value(100.0D)
            .defaultValue(100D)
            .min(0D)
            .max(100D)
            .roundingPlace(0)
            .shouldRender(() -> playSound.value)
            .build()
    );
    private static NotificationModule INSTANCE = new NotificationModule();


    protected NotificationModule() {
        super("Notifications", "Change notifications for certain events", Categories.MISC);
        addSettingGroup(sgNotifications);
        addSettingGroup(sgConfig);

        addQuickSettings(sgNotifications.getSettings());
        addQuickSettings(sgConfig.getSettings());
    }

    @Override
    public void onSettingChange(Setting<?> setting) {
        Notification.ANIMATE = Notification.AnimationStyle.values()[animationStyle.value];
        NotificationManager.setMaxDisplayed((int) maxNotifications.value);
    }

    public static NotificationModule get() {
        return INSTANCE;
    }
}
