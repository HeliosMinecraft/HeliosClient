package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.CycleSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.client.option.KeyBinding;

import java.util.List;

public class AutoWalk extends Module_ {

    public SettingGroup sgGeneral = new SettingGroup("General");
    CycleSetting walkDirection = sgGeneral.add(new CycleSetting.Builder()
            .name("Walk Direction")
            .description("Direction for player to walk in")
            .onSettingChange(this)
            .value(List.of(Direction.values()))
            .defaultListOption(Direction.Forward)
            .build()
    );

    DoubleSetting yaw = sgGeneral.add(new DoubleSetting.Builder()
            .name("Yaw of player")
            .description("Set the Yaw for the player to follow")
            .onSettingChange(this)
            .value(0.0)
            .min(0.0)
            .max(360.0d)
            .roundingPlace(1)
            .shouldRender(() -> walkDirection.getOption() == Direction.Custom)
            .build()
    );

    public AutoWalk() {
        super("AutoWalk", "Automatically walks for you", Categories.MOVEMENT);

        addSettingGroup(sgGeneral);

        addQuickSettings(sgGeneral.getSettings());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        resetAndPress(null);
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        switch ((Direction) walkDirection.getOption()) {
            case Forward -> resetAndPress(mc.options.forwardKey);
            case Backward -> resetAndPress(mc.options.backKey);
            case Left -> resetAndPress(mc.options.leftKey);
            case Right -> resetAndPress(mc.options.rightKey);
            case Custom -> {
                resetAndPress(null);

                double yawRadians = -Math.toRadians(yaw.value);

                double velX = mc.player.getMovementSpeed() * Math.sin(yawRadians) * (mc.player.isSprinting() ? 1.5f : 1f);
                double velZ = mc.player.getMovementSpeed() * Math.cos(yawRadians) * (mc.player.isSprinting() ? 1.5f : 1f);

                mc.player.setVelocity(velX, mc.player.getVelocity().y, velZ);
                mc.player.setYaw((float) yaw.value);
            }
        }
    }

    public void resetAndPress(KeyBinding key) {
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.rightKey.setPressed(false);

        if (key != null)
            key.setPressed(true);
    }

    public enum Direction {
        Forward,
        Left,
        Right,
        Backward,
        Custom
    }
}
