package dev.heliosclient.module.test;

import java.util.ArrayList;
import java.util.List;

public class ExampleModule extends Module {
    private BooleanSetting setting1;
    private BooleanSetting setting2;

    public ExampleModule() {
        super("Example Module", false);
        this.setting1 = new BooleanSetting("Setting 1", false);
        this.setting2 = new BooleanSetting("Setting 2", true);
        addSetting(setting1);
        addSetting(setting2);
    }
}

