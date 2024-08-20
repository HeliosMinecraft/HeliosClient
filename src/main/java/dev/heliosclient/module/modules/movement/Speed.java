package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.PlayerMotionEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.CycleSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class Speed extends Module_ {
    private final SettingGroup sgGeneral = new SettingGroup("General");
    CycleSetting speedMode = sgGeneral.add(new CycleSetting.Builder()
            .name("Speed Mode")
            .description("Change the player speed mode")
            .value(List.of(Modes.values()))
            .onSettingChange(this)
            .defaultListOption(Modes.OnGround)
            .build()
    );
    BooleanSetting alwaysJump = sgGeneral.add(new BooleanSetting.Builder()
            .name("Always Jump")
            .description("Jumps always when strafing. Otherwise it will only jump when specific conditions are met")
            .onSettingChange(this)
            .value(true)
            .shouldRender(()->speedMode.getOption() == Modes.Strafe)
            .build()
    );
    BooleanSetting whileSneaking = sgGeneral.add(new BooleanSetting.Builder()
            .name("While sneaking")
            .description("To apply speed modifier while sneaking")
            .onSettingChange(this)
            .value(false)
            .build()
    );

    DoubleSetting speed = sgGeneral.add(new DoubleSetting.Builder()
            .name("Speed")
            .description("Multiplier of speed.")
            .onSettingChange(this)
            .value(1.2)
            .defaultValue(1.2)
            .min(0.1)
            .max(100)
            .roundingPlace(1)
            .build()
    );

    public Speed() {
        super("Speed", "Allows you to move faster.", Categories.MOVEMENT);

        addSettingGroup(sgGeneral);

        addQuickSettings(sgGeneral.getSettings());
    }

    @SubscribeEvent
    @SuppressWarnings("all")
    public void onMotion(PlayerMotionEvent e) {
        if (mc.options.sneakKey.isPressed() && !whileSneaking.value)
            return;

        // Strafe Mode
        if (speedMode.getOption() == Modes.Strafe) {
                if (!mc.player.isSprinting()) {
                    mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
                }

                mc.player.setVelocity(new Vec3d(0, mc.player.getVelocity().y, 0));
                mc.player.updateVelocity((float) speed.value/4f, new Vec3d(mc.player.sidewaysSpeed, 0, mc.player.forwardSpeed));

                double vel = Math.abs(mc.player.getVelocity().getX()) + Math.abs(mc.player.getVelocity().getZ());

                if(alwaysJump.value && mc.player.isOnGround()){
                    mc.player.jump();
                }

                if (vel >= 0.12 && mc.player.isOnGround()) {
                    mc.player.updateVelocity(vel >= 0.3 ? 0.0f : 0.15f, new Vec3d(mc.player.sidewaysSpeed, 0, mc.player.forwardSpeed));
                    mc.player.jump();
                }
        }
        // OnGround Mode
        else if (speedMode.getOption() == Modes.OnGround) {
            if (mc.options.jumpKey.isPressed() || mc.player.isFallFlying())
                return;

            double speeds = 0.85 + speed.value / 30;

            if (mc.player.getY() >= mc.player.prevY + 0.399994D) {
                mc.player.setVelocity(mc.player.getVelocity().x, -0.9, mc.player.getVelocity().z);
                mc.player.setPos(mc.player.getX(), mc.player.prevY, mc.player.getZ());
            }

            if (mc.player.forwardSpeed != 0.0F && !mc.player.horizontalCollision) {
                if (mc.player.verticalCollision) {
                    mc.player.setVelocity(mc.player.getVelocity().x * speeds, mc.player.getVelocity().y, mc.player.getVelocity().z * speeds);
                }

                if (mc.player.getY() >= mc.player.prevY + 0.399994D) {
                    mc.player.setVelocity(mc.player.getVelocity().x, -100, mc.player.getVelocity().z);
                }
            }
        }
        // Bhop Mode
        else if (speedMode.getOption() == Modes.Bhop) {
            if (mc.player.forwardSpeed > 0 && mc.player.isOnGround()) {
                double speeds = 0.65 + speed.value / 30;

                mc.player.jump();
                mc.player.setVelocity(mc.player.getVelocity().x * speeds, 0.255556, mc.player.getVelocity().z * speeds);
                mc.player.sidewaysSpeed += 3.0F;
                mc.player.jump();
                mc.player.setSprinting(true);
            }
        }
        // Leap Mode
        else if (speedMode.getOption() == Modes.Leap) {
            if (mc.options.jumpKey.isPressed()) {
                double currentJumpHeight = mc.player.getVelocity().y;
                double newJumpHeight = Math.min(currentJumpHeight + 0.0351293, 0.5);
                mc.player.setVelocity(mc.player.getVelocity().x , newJumpHeight, mc.player.getVelocity().z);
            }
        }
        // SprintBoost Mode aka onGround but a bit different
        else if (speedMode.getOption() == Modes.SprintBoost) {
            if (mc.player.isSprinting()) {
                float yaw = (float) Math.toRadians(mc.player.getYaw());
                Vec3d forward = new Vec3d(-MathHelper.sin(yaw) * (speed.value / 10d), mc.player.getVelocity().y,
                        MathHelper.cos(yaw) * (speed.value / 10d));
                mc.player.setVelocity(forward);
            }
        }
    }


    public enum Modes {
        OnGround,
        Strafe,
        Bhop,
        Leap,
        SprintBoost,
    }

}
