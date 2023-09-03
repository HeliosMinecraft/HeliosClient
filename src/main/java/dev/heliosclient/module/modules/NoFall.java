package dev.heliosclient.module.modules;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.CycleSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.ArrayList;
import java.util.List;

public class NoFall extends Module_ {
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    public DoubleSetting fallHeight = new DoubleSetting("Trigger height", "Height on which No Fall triggers", this, 2.5, 2, 22, 1);
    public CycleSetting mode = new CycleSetting("Mode", "Mode which should save player from fall height ", this, new ArrayList<String>(List.of("Classic", "Disconnect (annoying)")), 0);

    public NoFall() {
        super("NoFall", "Prevents you from taking fall damage.", Category.PLAYER);

        settings.add(fallHeight);
        settings.add(mode);

        quickSettings.add(fallHeight);
        quickSettings.add(mode);
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        assert mc.player != null;
        if (mc.player.fallDistance >= fallHeight.value && !mc.player.isCreative()) {
            if (mode.value == 0) {
                mc.player.networkHandler.sendPacket(
                        new PlayerMoveC2SPacket.OnGroundOnly(true)
                );
            } else if (mode.value == 1) {
                int distance = 0;
                int y = (int) mc.player.getY();
                int maxDistance = y - 1;
                while (distance < maxDistance) {
                    if (!mc.player.clientWorld.isAir(mc.player.getBlockPos().down(distance + 1))) {
                        break;
                    }
                    distance++;
                }
                if (distance <= 2) {
                    assert mc.world != null;
                    mc.world.disconnect();
                }
            }
        }
    }
}
