package dev.heliosclient.module.modules;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.client.MinecraftClient;

public class Step extends Module_ {
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    private final SettingGroup sgGeneral = new SettingGroup("General");
    DoubleSetting stepHeight = sgGeneral.add(new DoubleSetting.Builder()
            .name("Height")
            .description("Height which step should step up at.")
            .module(this)
            .value(1.0)
            .defaultValue(1.0)
            .min(1.0)
            .max(10)
            .roundingPlace(1)
            .build()
    );
    BooleanSetting shiftSuppress = sgGeneral.add(new BooleanSetting.Builder()
            .name("Crouch suppress")
            .description("Disables step when crouch key is pressed.")
            .module(this)
            .value(true)
            .defaultValue(true)
            .build()
    );

    public Step() {
        super("Step", "Allows you to step up full blocks.", Categories.MOVEMENT);

        addSettingGroup(sgGeneral);

        addQuickSettingGroup(sgGeneral);
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
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player == null) {
            return;
        }
        mc.player.setStepHeight(0.5f);
    }
}
