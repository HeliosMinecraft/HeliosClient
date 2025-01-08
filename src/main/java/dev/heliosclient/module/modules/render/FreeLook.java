package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.input.MouseScrollEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;

public class FreeLook extends Module_ {
    public int distance = 0;

    public float cameraPitch = 0, cameraYaw = 0;
    Perspective previous = null;

    SettingGroup sgGeneral = new SettingGroup("General");

    public BooleanSetting togglePerspective = sgGeneral.add(new BooleanSetting.Builder()
            .name("Toggle Perspective")
            .description("Toggles perspective to F5 mode on module enable")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );

    public DoubleSetting sensitivity = sgGeneral.add(new DoubleSetting.Builder()
            .name("Look Sensitivity")
            .onSettingChange(this)
            .value(1d)
            .defaultValue(1d)
            .min(0)
            .max(3)
            .roundingPlace(2)
            .build()
    );

    public BooleanSetting scrollDistance = sgGeneral.add(new BooleanSetting.Builder()
            .name("Scroll Distance")
            .description("Distance amount changes with scrolling")
            .onSettingChange(this)
            .value(true)
            .build()
    );
    public DoubleSetting distanceFromPlayer = sgGeneral.add(new DoubleSetting.Builder()
            .name("Distance from player")
            .description("Sets the camera distance from player")
            .onSettingChange(this)
            .value(4.0)
            .defaultValue(4.0)
            .min(0)
            .max(200)
            .roundingPlace(0)
            .build());

    public FreeLook() {
        super("FreeLook", "Allows you to freely look around the player in third person", Categories.RENDER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());

    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.options != null && mc.player != null) {
            previous = mc.options.getPerspective();
            cameraPitch = mc.player.getYaw() + 45;
            cameraYaw = mc.player.getPitch() + 90;

            if (previous != Perspective.THIRD_PERSON_BACK && togglePerspective.value) {
                mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
            }
        }
        distance = (int) distanceFromPlayer.value;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.options.getPerspective() != previous && togglePerspective.value) {
            mc.options.setPerspective(previous);
        }

    }

    @SubscribeEvent
    public void onTick(TickEvent.CLIENT event) {
        if (!scrollDistance.value) {
            distance = (int) distanceFromPlayer.value;
        }
    }

    @SubscribeEvent
    public void onScroll(MouseScrollEvent event) {
        if (mc.currentScreen != null) return;

        if (scrollDistance.value) {
            distance -= (int) (event.getVerticalAmount() * mc.options.getMouseWheelSensitivity().getValue() * 1.75f);
            distance = MathHelper.clamp(distance, 0, 200);
            event.cancel();
        }
    }

    public int getDistanceFromPlayer() {
        return distance;
    }
}
