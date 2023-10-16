package dev.heliosclient.module.modules;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.ColorSetting;
import dev.heliosclient.module.settings.SettingBuilder;
import dev.heliosclient.ui.HUDOverlay;
import dev.heliosclient.util.ColorUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.awt.*;

public class HUDModule extends Module_ {
    private final SettingBuilder sgGeneral = new SettingBuilder("General");
    public BooleanSetting clientTag = sgGeneral.add(new BooleanSetting.Builder()
            .name("Client Tag")
            .description("Visibility of Client Tag.")
            .module(this)
            .value(true)
            .build()
    );
    public ColorSetting colorSetting = sgGeneral.add(new ColorSetting.Builder()
            .name("Color")
            .description("Color of HUD.")
            .module(this)
            .value(new Color(241, 83, 92, 255).getRGB())
            .build()
    );
    BooleanSetting Rainbow = sgGeneral.add(new BooleanSetting.Builder()
            .name("Rainbow")
            .description("Toggles rainbow effect for HUD.")
            .module(this)
            .value(false)
            .build()
    );


    public HUDModule() {
        super("HUD", "The HeliosClient HUD. Toggle to update.", Category.RENDER);
        this.active.value = true;
        this.showInModulesList.value = false;

        settingBuilders.add(sgGeneral);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        HUDOverlay.INSTANCE.showClientTag = clientTag.value;
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (Rainbow.value) {
                HeliosClient.uiColorA = ColorUtils.getRainbowColor().getRGB();
                HeliosClient.uiColor = ColorUtils.getRainbowColor().getRGB();
            } else {
                HeliosClient.uiColorA = colorSetting.value;
                HeliosClient.uiColor = colorSetting.value;
            }
        });
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        HUDOverlay.INSTANCE.showClientTag = clientTag.value;
        if (!Rainbow.value) {
            HeliosClient.uiColorA = colorSetting.value;
            HeliosClient.uiColor = colorSetting.value;
        }
    }
}
