package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.mixin.AccessorKeybind;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import dev.heliosclient.ui.clickgui.settings.AbstractSettingScreen;
import dev.heliosclient.ui.clickgui.hudeditor.HudEditorScreen;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class GuiMove extends Module_ {
    public GuiMove() {
        super("Gui Move", "Listens to input keys even while in screens", Categories.MOVEMENT);
    }

    public boolean dontMove() {
        return mc.currentScreen == null || mc.currentScreen instanceof AbstractCommandBlockScreen || mc.currentScreen instanceof CreativeInventoryScreen || mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof AnvilScreen || mc.currentScreen instanceof StructureBlockScreen || mc.currentScreen instanceof SignEditScreen || ClickGUIScreen.INSTANCE.searchBar.isFocused() || mc.currentScreen instanceof AbstractSettingScreen || mc.currentScreen instanceof HudEditorScreen;
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if (dontMove()) return;

        setPress(mc.options.forwardKey);
        setPress(mc.options.backKey);
        setPress(mc.options.rightKey);
        setPress(mc.options.leftKey);
        setPress(mc.options.sneakKey);
        setPress(mc.options.jumpKey);
        setPress(mc.options.sprintKey);
    }

    private void setPress(KeyBinding bind) {
        bind.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), ((AccessorKeybind) bind).getKey().getCode()));
    }
}
