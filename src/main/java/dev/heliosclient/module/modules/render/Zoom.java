package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.input.MouseScrollEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.TimerUtils;
import dev.heliosclient.util.animation.Easing;
import dev.heliosclient.util.animation.EasingType;
import net.minecraft.util.math.MathHelper;

public class Zoom extends Module_ {
    TimerUtils timer = new TimerUtils();
    SettingGroup sgGeneral = new SettingGroup("General");
    public BooleanSetting scrollZoom = sgGeneral.add(new BooleanSetting.Builder()
            .name("Scroll Zoom")
            .description("Zoom amount changes with scrolling")
            .onSettingChange(this)
            .value(true)
            .build()
    );
    DoubleSetting amount = sgGeneral.add(new DoubleSetting.Builder()
            .name("Zoom Amount")
            .description("Amount of zoom to apply")
            .onSettingChange(this)
            .min(1.0)
            .max(108)
            .value(50.0)
            .roundingPlace(1)
            .shouldRender(() -> !scrollZoom.value)
            .build()
    );
    private double zoomAmount = 1;
    private int prevFOV = 70;

    public Zoom() {
        super("Zoom", "Zooooooom", Categories.RENDER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.options != null)
            prevFOV = mc.options.getFov().getValue();
        timer.startTimer();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.options.getFov().setValue(prevFOV);
        timer.resetTimer();
    }

    @SubscribeEvent
    public void onScroll(MouseScrollEvent event) {
        if (mc.currentScreen != null) return;

        if (scrollZoom.value) {
            zoomAmount -= (event.getVerticalAmount() * mc.options.getMouseWheelSensitivity().getValue() * 1.25f);
            zoomAmount = MathHelper.clamp(zoomAmount, 1, 110);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (!scrollZoom.value) {
            float t = Math.min(1.0f, (float) timer.getElapsedTime()); // Normalize time to [0, 1]

            // Apply easing to zoom amount
            zoomAmount = Easing.ease(EasingType.LINEAR_IN, t) * amount.value;
        }
    }

    public double getZoomAmount() {
        return zoomAmount;
    }
}
