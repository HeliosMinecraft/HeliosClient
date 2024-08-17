package dev.heliosclient.module.modules.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

public class XCarry extends Module_ {
    public XCarry() {
        super("XCarry","eXtra carry in your crafting and other slots", Categories.PLAYER);
    }

    @SubscribeEvent
    public void onPaketSend(PacketEvent.SEND e) {
        if (e.packet instanceof CloseHandledScreenC2SPacket)
            e.setCanceled(true);
    }
}