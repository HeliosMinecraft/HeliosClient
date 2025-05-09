package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.SettingGroup;

public class Test2 extends Module_ {
    private final SettingGroup sgGeneral = new SettingGroup("General");


    public Test2() {
        super("Test 2", "Render Test 2", Categories.RENDER);
        addSettingGroup(sgGeneral);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player == null) return;
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
    @SubscribeEvent
    public void onPackRec(PacketEvent.RECEIVE e){}
}