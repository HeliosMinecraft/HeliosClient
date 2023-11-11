package dev.heliosclient.module.modules;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.RGBASetting;
import dev.heliosclient.module.settings.SettingGroup;

import java.awt.*;

public class HUDModule extends Module_ implements Listener {
    private final SettingGroup sgGeneral = new SettingGroup("General");
    public RGBASetting colorSetting = sgGeneral.add(new RGBASetting.Builder()
            .name("Color")
            .description("Color of HUD.")
            .module(this)
            .value(new Color(241, 83, 92, 255))
            .defaultValue(new Color(241, 83, 92, 255))
            .build()
    );

    public HUDModule() {
        super("HUD", "The HeliosClient HUD. Toggle to update.", Categories.RENDER);
        this.active.value = true;
        this.showInModulesList.value = false;
        addSettingGroup(sgGeneral);
        HeliosClient.uiColorA = colorSetting.getColor().getAlpha();
        HeliosClient.uiColor = colorSetting.getColor().getRGB();
        EventManager.register(this);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @SubscribeEvent
    public void onTick(TickEvent.CLIENT event) {
        HeliosClient.uiColorA = colorSetting.getColor().getAlpha();
        HeliosClient.uiColor = colorSetting.getColor().getRGB();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        HeliosClient.uiColorA = colorSetting.getColor().getAlpha();
        HeliosClient.uiColor = colorSetting.getColor().getRGB();
    }
}
