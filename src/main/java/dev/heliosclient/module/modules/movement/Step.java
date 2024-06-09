package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;

public class Step extends Module_ {
    private final SettingGroup sgGeneral = new SettingGroup("General");
    DoubleSetting stepHeight = sgGeneral.add(new DoubleSetting.Builder()
            .name("Height")
            .description("Height which step should step up at.")
            .onSettingChange(this)
            .value(1.0)
            .defaultValue(1.0)
            .min(0.0)
            .max(10)
            .roundingPlace(1)
            .build()
    );
    BooleanSetting shiftSuppress = sgGeneral.add(new BooleanSetting.Builder()
            .name("Crouch suppress")
            .description("Disables step when crouch key is pressed.")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .build()
    );
    BooleanSetting reverseStep = sgGeneral.add(new BooleanSetting.Builder()
            .name("Reverse Step")
            .description("Allows you to step down faster")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .build()
    );

    private float previousHeight = 0.5f;

    public Step() {
        super("Step", "Allows you to step up full blocks.", Categories.MOVEMENT);

        addSettingGroup(sgGeneral);

        addQuickSettings(sgGeneral.getSettings());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player != null)
            previousHeight = mc.player.getStepHeight();
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if (mc.player == null) return;
        if (shiftSuppress.value) {
            if (mc.options.sneakKey.isPressed()) {
                mc.player.setStepHeight(0.5f);
            } else {
                mc.player.setStepHeight((float) stepHeight.value);
            }
        } else {
            mc.player.setStepHeight((float) stepHeight.value);
        }

        if (reverseStep.value) {
            if (mc.player.isInLava() || mc.player.isTouchingWater() || !mc.player.isOnGround() || mc.player.isFallFlying())
                return;
            mc.player.addVelocity(0, -1, 0);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player == null) {
            return;
        }
        mc.player.setStepHeight(previousHeight);
    }
}
