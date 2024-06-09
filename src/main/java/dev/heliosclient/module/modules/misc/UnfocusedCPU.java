package dev.heliosclient.module.modules.misc;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;

public class UnfocusedCPU extends Module_ {
    int prevFPS;

    SettingGroup sgGeneral = new SettingGroup("General");
    DoubleSetting fps = sgGeneral.add(new DoubleSetting.Builder()
            .name("FPS")
            .description("FPS to limit to.")
            .onSettingChange(this)
            .value(10d)
            .defaultValue(10d)
            .min(10)
            .max(20)
            .roundingPlace(0)
            .build()
    );

    public UnfocusedCPU() {
        super("UnfocusedCPU", "Changes frame limit to low levels when minecraft unfocused to reduce memory usage", Categories.MISC);
        addSettingGroup(sgGeneral);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.options != null) {
            prevFPS = mc.options.getMaxFps().getValue();
        } else {
            prevFPS = 200;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (!mc.isWindowFocused()) {
            mc.options.getMaxFps().setValue((int) fps.value);
        } else {
            mc.options.getMaxFps().setValue(prevFPS);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.options.getMaxFps().setValue(prevFPS);
    }
}
