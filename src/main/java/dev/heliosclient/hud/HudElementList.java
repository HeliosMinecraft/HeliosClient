package dev.heliosclient.hud;

import dev.heliosclient.hud.hudelements.*;
import dev.heliosclient.ui.clickgui.hudeditor.HudCategoryPane;
import net.minecraft.screen.GenericContainerScreenHandler;

import java.util.Map;
import java.util.TreeMap;

public class HudElementList {
    public static HudElementList INSTANCE;
    public final Map<String, HudElementData<?>> elementDataMap = new TreeMap<>();

    public HudElementList() {
        registerElement(WelcomeHud.DATA);
        registerElement(ClientTag.DATA);
        registerElement(ModuleList.DATA);
        registerElement(Radar.DATA);
        registerElement(CompassHud.DATA);
        registerElement(CompactData.DATA);
        registerElement(PlayerModel.DATA);
        registerElement(CoordinatesHud.DATA);
        registerElement(Fps.DATA);
        registerElement(Bps.DATA);
        registerElement(Ping.DATA);
        registerElement(Tps.DATA);
    }

    private void registerElement(HudElementData<?> hudElementData) {
        elementDataMap.put(hudElementData.name(), hudElementData);
    }

    public void registerElements(HudElementData<?>... datas) {
        for (HudElementData<?> module : datas) {
            elementDataMap.put(module.name(), module);
        }
        HudCategoryPane.INSTANCE = new HudCategoryPane();
    }
}
