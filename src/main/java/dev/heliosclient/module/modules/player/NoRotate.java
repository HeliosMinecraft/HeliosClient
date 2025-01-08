package dev.heliosclient.module.modules.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.mixin.AccessorPlayerPosition;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;

public class NoRotate extends Module_ {
    public NoRotate() {
        super("NoRotate", "Tries to overwrite rotation values sent from server to client", Categories.PLAYER);
    }

    @SubscribeEvent(priority = SubscribeEvent.Priority.LOW)
    private void onPacketReceive(PacketEvent.RECEIVE event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket ppl) {
            PlayerPosition position2 = ppl.change();
            AccessorPlayerPosition accessor = (AccessorPlayerPosition) (Object) position2;
            assert accessor != null;
            accessor.setPitch(mc.player.getPitch());
            accessor.setYaw(mc.player.getYaw());
            ppl.relatives().remove(PositionFlag.X_ROT);
            ppl.relatives().remove(PositionFlag.Y_ROT);
        }
    }
}
