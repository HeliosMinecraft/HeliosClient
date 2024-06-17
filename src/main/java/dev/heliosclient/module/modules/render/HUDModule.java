package dev.heliosclient.module.modules.render;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.RGBASetting;
import dev.heliosclient.module.settings.SettingGroup;

import java.awt.*;

public class HUDModule extends Module_ implements Listener {
    private final SettingGroup sgGeneral = new SettingGroup("General");
    public BooleanSetting accentColor = sgGeneral.add(new BooleanSetting.Builder()
            .name("Use Accent Color")
            .description("Uses the accent color of the client as the hud color")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    public RGBASetting colorSetting = sgGeneral.add(new RGBASetting.Builder()
            .name("Color")
            .description("Color of HUD.")
            .onSettingChange(this)
            .value(new Color(241, 83, 92, 255))
            .defaultValue(new Color(241, 83, 92, 255))
            .shouldRender(() -> !accentColor.value)
            .build()
    );

    public HUDModule() {
        super("HUD", "The HeliosClient HUD. Toggle to update.", Categories.RENDER);
        this.active.value = true;
        this.showInModulesList.value = false;

        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());

        EventManager.register(this);
        updateUIColor();
    }

    public void updateUIColor() {
        if ((accentColor.value && HeliosClient.CLICKGUI != null) || ColorManager.SYNC_ACCENT) {
            ColorManager.INSTANCE.hudColor = HeliosClient.CLICKGUI.getAccentColor();
        } else {
            ColorManager.INSTANCE.hudColor = colorSetting.getColor().getRGB();
        }
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
    public void onTickClient(TickEvent e) {
        updateUIColor();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateUIColor();
    }
}
