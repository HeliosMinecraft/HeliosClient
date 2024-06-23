package dev.heliosclient.module.settings;

import net.minecraft.client.gui.screen.Screen;

import java.util.function.BooleanSupplier;

public class ParentScreenSetting<T> extends Setting<T>{
    public Screen parentScreen = null;

    public ParentScreenSetting(BooleanSupplier shouldRender, T defaultValue) {
        super(shouldRender, defaultValue);
    }

    public Screen getParentScreen() {
        return parentScreen;
    }

    public void setParentScreen(Screen parentScreen) {
        this.parentScreen = parentScreen;
    }
}
