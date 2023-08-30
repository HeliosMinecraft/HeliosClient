package dev.heliosclient.hud;

import dev.heliosclient.hud.hudelements.Coords;

import java.util.ArrayList;

public class HudElementList {
    public static HudElementList INSTANCE = new HudElementList();
    public ArrayList<HudElement> hudElements = new ArrayList<>();

    public HudElementList() {
        registerElements(new Coords());
    }

    public void registerElement(HudElement module) {
        hudElements.add(module);
    }

    public void registerElements(HudElement... modules) {
        for (HudElement module : modules) {
            this.hudElements.add(module);
            module.onLoad();
        }
    }
}
