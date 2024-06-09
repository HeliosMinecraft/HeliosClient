package dev.heliosclient.module.modules.misc;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.client.ServerConnectHeadEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.client.network.ServerInfo;

public class AutoReconnect extends Module_ {
    public ServerInfo lastConnection;
    SettingGroup sgGeneral = new SettingGroup("General");
    public DoubleSetting delay = sgGeneral.add(new DoubleSetting.Builder()
            .name("Reconnect delay")
            .description("Reconnect delay (in seconds)")
            .min(0)
            .max(120)
            .value(5d)
            .defaultValue(5d)
            .roundingPlace(1)
            .onSettingChange(this)
            .build()
    );


    public AutoReconnect() {
        super("AutoReconnect", "Automatically connects to the last disconnected server", Categories.MISC);
        addSettingGroup(sgGeneral);

        addQuickSetting(delay);
    }

    @SubscribeEvent
    public void onConnect(ServerConnectHeadEvent event) {
        lastConnection = event.info;
    }
}
