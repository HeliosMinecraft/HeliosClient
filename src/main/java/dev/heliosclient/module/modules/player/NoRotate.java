package dev.heliosclient.module.modules.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.mixin.AccessorPlayerPositionLookS2CPacket;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class NoRotate extends Module_ {
    public NoRotate() {
        super("NoRotate", "Tries to overwrite rotation values sent from server to client", Categories.PLAYER);
    }

    @SubscribeEvent
    private void onPacketReceive(PacketEvent.RECEIVE event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket) {
            ((AccessorPlayerPositionLookS2CPacket) event.packet).setPitch(mc.player.getPitch());
            ((AccessorPlayerPositionLookS2CPacket) event.packet).setYaw(mc.player.getYaw());
        }
    }
}
