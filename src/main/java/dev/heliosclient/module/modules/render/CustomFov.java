package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.interfaces.ISimpleOption;

public class CustomFov extends Module_ {
    private final SettingGroup sgGeneral = new SettingGroup("General");
    DoubleSetting FOV = sgGeneral.add(new DoubleSetting.Builder()
            .name("FOV")
            .description("Desired FOV")
            .onSettingChange(this)
            .value(140.0)
            .defaultValue(140.0)
            .min(1)
            .max(240)
            .roundingPlace(0)
            .build()
    );

    private int previousFov = 100;

    public CustomFov() {
        super("CustomFOV", "Allows you to set custom field of view.", Categories.RENDER);
        addSettingGroup(sgGeneral);

        addQuickSetting(FOV);
    }

    @SubscribeEvent
    public void onTick(TickEvent.CLIENT event) {
        if (mc.options == null) return;

        ((ISimpleOption<Integer>) (Object) mc.options.getFov()).heliosClient$setValueUnrestricted((int) (FOV.value));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.options == null && mc.options.getFov().getValue() <= 110) return;

        previousFov = mc.options.getFov().getValue();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.options == null && mc.options.getFov().getValue() <= 110) return;

        ((ISimpleOption<Integer>) (Object) mc.options.getFov()).heliosClient$setValueUnrestricted(previousFov);
    }
}
