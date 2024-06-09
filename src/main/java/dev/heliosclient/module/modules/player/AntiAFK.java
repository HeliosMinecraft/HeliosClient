package dev.heliosclient.module.modules.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.random.Random;

public class AntiAFK extends Module_ {
    int actionTimer = 0;
    boolean isSneaking = false;

    SettingGroup sgGeneral = new SettingGroup("General");

    BooleanSetting autoJump = sgGeneral.add(new BooleanSetting("AutoJump", "Jumps in between delays", this, true, () -> true, true));
    BooleanSetting swingHand = sgGeneral.add(new BooleanSetting("SwingHand", "Swings hand in between delays", this, true, () -> true, true));
    BooleanSetting rotate = sgGeneral.add(new BooleanSetting("AutoRotate", "Rotates the player to look at a random degree", this, false, () -> true, true));
    BooleanSetting sneak = sgGeneral.add(new BooleanSetting("AutoSneak", "Sneaks in between delays", this, false, () -> true, true));

    DoubleSetting delay = sgGeneral.add(new DoubleSetting.Builder()
            .name("Delay")
            .description("The delay (in ticks) to perform the afk actions")
            .onSettingChange(this)
            .min(0)
            .max(1200)
            .defaultValue(100d)
            .value(100d)
            .roundingPlace(0)
            .build()
    );


    public AntiAFK() {
        super("AntiAFK", "Automatically does actions for you to prevent being afk kicked", Categories.PLAYER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        actionTimer = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (isSneaking) {
            isSneaking = false;
            mc.options.sneakKey.setPressed(false);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        actionTimer++;

        if (actionTimer > delay.value) {
            actionTimer = 0;

            if (autoJump.value) {
                mc.player.jump();
            }
            if (swingHand.value) {
                mc.player.swingHand(Hand.MAIN_HAND);
            }
            if (sneak.value) {
                if (isSneaking) {
                    mc.options.sneakKey.setPressed(false);
                    isSneaking = false;
                } else {
                    mc.options.sneakKey.setPressed(true);
                    isSneaking = true;
                }
            }
            if (rotate.value) {
                setRandomPitchAndYaw(mc.player);
            }

        }
    }

    public void setRandomPitchAndYaw(PlayerEntity player) {
        Random random = Random.create();

        // Generate a random pitch and yaw between -180 and 180
        float pitch = (random.nextFloat() - 0.5f) * 180.0f;
        float yaw = (random.nextFloat() - 0.5f) * 360.0f;

        player.setYaw(yaw);
        player.setPitch(pitch);
    }
}
