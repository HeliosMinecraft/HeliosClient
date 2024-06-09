package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.DropDownSetting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class Fly extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");
    DropDownSetting mode = sgGeneral.add(new DropDownSetting.Builder()
            .name("Mode")
            .description("Change flight mode")
            .onSettingChange(this)
            .value(List.of(Mode.values()))
            .defaultListOption(Mode.Creative)
            .build()
    );
    DoubleSetting speed = sgGeneral.add(new DoubleSetting.Builder()
            .name("Speed")
            .description("Flight Speed")
            .onSettingChange(this)
            .value(1d)
            .defaultValue(1d)
            .min(0)
            .max(10)
            .roundingPlace(1)
            .build()
    );
    BooleanSetting bypassAntiKick = sgGeneral.add(new BooleanSetting.Builder()
            .name("Bypass anti kick")
            .description("Should attempt to bypass basic anti kick")
            .onSettingChange(this)
            .value(true)
            .build()
    );

    public Fly() {
        super("Fly", "Allows you to fly in survival mode.", Categories.MOVEMENT);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }


    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (mc.player != null) {
            if (mc.player.isSpectator() || mc.player.getAbilities().creativeMode) return;
            mc.player.getAbilities().flying = false;
            mc.player.getAbilities().allowFlying = false;
            mc.player.getAbilities().setFlySpeed(0.5f);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if (mc.player == null) return;

        switch ((Mode) mode.getOption()) {
            case Creative -> {
                if (mc.player.isSpectator() || mc.player.getAbilities().creativeMode) return;
                mc.player.getAbilities().flying = true;
                mc.player.getAbilities().setFlySpeed(((float) speed.value / 10f));
                mc.player.getAbilities().allowFlying = true;
            }
            case Velocity -> {
                mc.player.setVelocity(0, 0, 0);
                mc.player.getAbilities().flying = false;

                Vec3d pv = mc.player.getVelocity();
                if (mc.options.sneakKey.isPressed())
                    pv = pv.subtract(0, speed.value, 0);
                if (mc.options.jumpKey.isPressed())
                    pv = pv.add(0, speed.value, 0);
                mc.player.setVelocity(pv);
            }
        }
        if (bypassAntiKick.value && mc.player.age % 40 == 0) {
            mc.player.setVelocity(mc.player.getVelocity().x, -0.04, mc.player.getVelocity().z);
        }
    }

    public enum Mode {
        Creative,
        Velocity,
        Packet
    }
}
