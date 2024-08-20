package dev.heliosclient.module.modules.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.CycleSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class AutoClicker extends Module_ {

    public int leftClickTimer, rightClickTimer, middleClickTimer;

    SettingGroup sgGeneral = new SettingGroup("General");
    BooleanSetting showNotification = sgGeneral.add(new BooleanSetting.Builder()
            .name("Show Notification")
            .description("Whether or not to show notification for this module")
            .onSettingChange(this)
            .value(true)
            .build()
    );
    CycleSetting mode = sgGeneral.add(new CycleSetting.Builder()
            .name("ClickMode")
            .description("Mode of mouse button to click")
            .onSettingChange(this)
            .value(List.of(ClickMode.values()))
            .defaultListOption(ClickMode.LEFT)
            .build()
    );
    DoubleSetting delayLeft = sgGeneral.add(new DoubleSetting.Builder()
            .name("Left Click Delay")
            .description("Left Click Delay (in ticks)")
            .onSettingChange(this)
            .defaultValue(20d)
            .value(20d)
            .min(0)
            .max(200)
            .roundingPlace(0)
            .shouldRender(() -> mode.getOption() == ClickMode.LEFT || mode.getOption() == ClickMode.BOTH)
            .build()
    );
    DoubleSetting delayRight = sgGeneral.add(new DoubleSetting.Builder()
            .name("Right Click Delay")
            .description("Right Click Delay (in ticks)")
            .onSettingChange(this)
            .defaultValue(20d)
            .value(20d)
            .min(0)
            .max(200)
            .roundingPlace(0)
            .shouldRender(() -> mode.getOption() == ClickMode.RIGHT || mode.getOption() == ClickMode.BOTH)
            .build()
    );
    DoubleSetting delayMiddleClick = sgGeneral.add(new DoubleSetting.Builder()
            .name("Middle Click Delay")
            .description("Middle Click Delay (in ticks)")
            .onSettingChange(this)
            .defaultValue(20d)
            .value(20d)
            .min(0)
            .max(200)
            .roundingPlace(0)
            .shouldRender(() -> mode.getOption() == ClickMode.MIDDLE_CLICK)
            .build()
    );


    public AutoClicker() {
        super("AutoClicker", "Clicks buttons for you", Categories.PLAYER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        leftClickTimer = 0;
        middleClickTimer = 0;
        rightClickTimer = 0;
    }

    @Override
    public void sendNotification(boolean enabled) {
        if(showNotification.value) {
            super.sendNotification(enabled);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.CLIENT event) {
        if (mode.isOption(ClickMode.LEFT) || mode.isOption(ClickMode.BOTH)) {
            if (leftClickTimer >= delayLeft.value) {
                leftClickTimer = 0;
                setKeyPress(GLFW.GLFW_MOUSE_BUTTON_1);
            } else {
                leftClickTimer++;
            }
        }
        if (mode.isOption(ClickMode.RIGHT) || mode.isOption(ClickMode.BOTH)) {
            if (rightClickTimer >= delayRight.value) {
                rightClickTimer = 0;
                setKeyPress(GLFW.GLFW_MOUSE_BUTTON_2);
            } else {
                rightClickTimer++;
            }
        }

        if (mode.isOption(ClickMode.MIDDLE_CLICK)) {
            if (middleClickTimer >= delayMiddleClick.value) {
                middleClickTimer = 0;
                setKeyPress(GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
            } else {
                middleClickTimer++;
            }
        }
    }

    public void setKeyPress(int i) {
        KeyBinding.onKeyPressed(InputUtil.Type.MOUSE.createFromCode(i));
    }


    public enum ClickMode {
        LEFT,
        RIGHT,
        BOTH,
        MIDDLE_CLICK
    }
}
