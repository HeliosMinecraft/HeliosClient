package dev.heliosclient.module.modules.misc;

import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.DropDownSetting;
import dev.heliosclient.module.settings.SettingGroup;

import java.util.List;

public class NoSwing extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");
    public DropDownSetting swingMode = sgGeneral.add(new DropDownSetting.Builder()
            .name("Swing Mode")
            .description("Which hand to swing")
            .value(List.of(SwingMode.values()))
            .defaultValue(List.of(SwingMode.values()))
            .defaultListOption(SwingMode.None)
            .addOptionToolTip("Sets your hand swings to always main hand")
            .addOptionToolTip("Sets your hand swings to always offhand hand")
            .addOptionToolTip("Sets your hand swings to neither of the hand (only visually)")
            .addOptionToolTip("Sets your hand swings to neither of the hand (no packets to server as well)")
            .onSettingChange(this)
            .build()
    );

    public NoSwing() {
        super("NoSwing", "Modifies client and server hand swings", Categories.MISC);
        addSettingGroup(sgGeneral);

        addQuickSettings(sgGeneral.getSettings());

    }

    public enum SwingMode {
        MainHand,
        OffHand,
        None,
        NoServer,
    }
}
