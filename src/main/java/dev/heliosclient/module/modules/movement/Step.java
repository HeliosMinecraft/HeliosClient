package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;

public class Step extends Module_ {
    private final SettingGroup sgStep = new SettingGroup("Step");
    private final SettingGroup sgReverseStep = new SettingGroup("ReverseStep");

    DoubleSetting stepHeight = sgStep.add(new DoubleSetting.Builder()
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
    BooleanSetting shiftSuppress = sgStep.add(new BooleanSetting.Builder()
            .name("Crouch suppress")
            .description("Disables step when crouch key is pressed.")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .build()
    );
    BooleanSetting reverseStep = sgReverseStep.add(new BooleanSetting.Builder()
            .name("Reverse Step")
            .description("Allows you to step down faster")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .build()
    );
    DoubleSetting reverseStepMotion = sgReverseStep.add(new DoubleSetting.Builder()
            .name("Reverse Step Motion")
            .description("Motion factor of pull down")
            .onSettingChange(this)
            .value(0.7)
            .defaultValue(0.7)
            .min(0.0)
            .max(1.5)
            .roundingPlace(1)
            .build()
    );

    private float previousHeight = 0.5f;

    public Step() {
        super("Step", "Allows you to step up full blocks.", Categories.MOVEMENT);

        addSettingGroup(sgStep);
        addSettingGroup(sgReverseStep);

        addQuickSettings(sgStep.getSettings());
        addQuickSettings(sgReverseStep.getSettings());
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
            mc.player.addVelocity(0, -reverseStepMotion.value, 0);
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
