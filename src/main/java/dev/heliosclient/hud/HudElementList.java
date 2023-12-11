package dev.heliosclient.hud;

import dev.heliosclient.hud.hudelements.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HudElementList {
    public static HudElementList INSTANCE = new HudElementList();
    public ArrayList<HudElement> hudElements = new ArrayList<>();
    public HudElementList() {
        registerElement(new Coords());
        registerElement(new Fps());
        registerElement(new PlayerModel());
        registerElement(new ClientTag());
        registerElement(new Bps());
        registerElement(new Ping());
        registerElement(new ModuleList());
        registerElement(new TestHud());
    }

    public void registerElement(HudElement module) {
        hudElements.add(module);
        module.onLoad();
    }

    public void registerElements(HudElement... modules) {
        for (HudElement module : modules) {
            this.hudElements.add(module);
            module.onLoad();
        }
    }
}
