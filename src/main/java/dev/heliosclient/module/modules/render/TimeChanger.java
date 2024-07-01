package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public class TimeChanger extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");
    long prevTime;

    DoubleSetting time = sgGeneral.add(new DoubleSetting.Builder()
            .name("Time")
            .description("Time of world")
            .onSettingChange(this)
            .value(0d)
            .defaultValue(0d)
            .range(-20000, 20000)
            .roundingPlace(0)
            .build()
    );

    public TimeChanger() {
        super("TimeChanger", "Changes world time", Categories.RENDER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());

    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.world != null)
            prevTime = mc.world.getTime();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (mc.world != null)
            mc.world.setTimeOfDay(prevTime);
    }

    @SubscribeEvent
    private void onPacketReceive(PacketEvent.RECEIVE event) {
        if (event.packet instanceof WorldTimeUpdateS2CPacket packet) {
            prevTime = packet.getTime();
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.CLIENT e) {
        if (mc.world != null)
            mc.world.setTimeOfDay((long) time.value);
    }

    @Override
    public void onSettingChange(Setting<?> setting) {
        super.onSettingChange(setting);

        if (mc.world != null)
            mc.world.setTimeOfDay((long) time.value);
    }
}
