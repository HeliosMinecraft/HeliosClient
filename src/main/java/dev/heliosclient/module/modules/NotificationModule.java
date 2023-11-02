package dev.heliosclient.module.modules;

import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;

public class NotificationModule extends Module_ {
    private static final SettingGroup sgNotifications = new SettingGroup("Notifications");
    public static NotificationModule INSTANCE = new NotificationModule();
    public static BooleanSetting moduleNotification = sgNotifications.add(new BooleanSetting.Builder()
            .name("Module Info")
            .description("Whether to show module state.")
            .module(INSTANCE)
            .value(true)
            .defaultValue(true)
            .build()
    );

    public NotificationModule() {
        super("Notifications", "Change notifications for certain events", Categories.MISC);
        addSettingGroup(sgNotifications);
        addQuickSettingGroup(sgNotifications);
    }

    @Override
    public boolean isActive() {
        return super.isActive();
    }
}
