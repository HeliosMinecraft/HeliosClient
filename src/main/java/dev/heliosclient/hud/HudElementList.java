package dev.heliosclient.hud;

import dev.heliosclient.hud.hudelements.*;

import java.util.Map;
import java.util.TreeMap;

public class HudElementList {
    public static HudElementList INSTANCE;
    public final Map<String, HudElementData<?>> elementDataMap = new TreeMap<>();

    public HudElementList() {
        registerElement(Coords.DATA);
        registerElement(Fps.DATA);
        registerElement(PlayerModel.DATA);
        registerElement(ClientTag.DATA);
        registerElement(Bps.DATA);
        registerElement(Ping.DATA);
        registerElement(ModuleList.DATA);
        registerElement(TestHud.DATA);
        registerElement(CompactData.DATA);

    }

    public void registerElement(HudElementData hudElementData) {
        elementDataMap.put(hudElementData.name(), hudElementData);
    }

    public void registerElements(HudElementData... datas) {
        for (HudElementData module : datas) {
            elementDataMap.put(module.name(), module);
        }
    }
}
