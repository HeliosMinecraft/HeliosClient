package dev.heliosclient.hud;

import java.util.function.Supplier;

public record HudElementData<T extends HudElement>(String name, String description, Supplier<T> elementFactory) {
    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return description;
    }
    public HudElement create() {
        return elementFactory.get();
    }

}