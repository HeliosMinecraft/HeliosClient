package dev.heliosclient.module.modules.misc;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

public class SilentClose extends Module_ {
    public SilentClose() {
        super("SilentClose", "Silently closes containers to make the server think you are still in a container", Categories.MISC);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.SEND event) {
        if (event.packet instanceof CloseHandledScreenC2SPacket) {
            event.setCanceled(true);
        }
    }
}