package dev.heliosclient.module.modules.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.DisconnectEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PingSpoof extends Module_ {
    /**
     * Queue of packets to preserve their order
     */
    protected final Queue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    SettingGroup sgGeneral = new SettingGroup("General");
    public DoubleSetting delay = sgGeneral.add(new DoubleSetting.Builder()
            .name("Delay")
            .onSettingChange(this)
            .defaultValue(100d)
            .value(100d)
            .min(0d)
            .max(4500)
            .roundingPlace(0)
            .build()
    );
    public BooleanSetting keepAlivePackets = sgGeneral.add(new BooleanSetting.Builder()
            .name("KeepAlive Packets")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    public BooleanSetting resourcesPackets = sgGeneral.add(new BooleanSetting.Builder()
            .name("Resources Packets")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );


    public PingSpoof() {
        super("PingSpoof", "Spoofs your ping to the server", Categories.PLAYER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        clearPackets(true);
    }

    @SubscribeEvent
    public void onLeave(DisconnectEvent event) {
        clearPackets(false);
    }

    public void clearPackets(boolean send) {
        packets.removeIf(packet -> {
            if (send && mc.player != null && mc.player.networkHandler != null) {
                mc.player.networkHandler.sendPacket(packet);
            }
            return true;
        });
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.SEND event) {
        if (keepAlivePackets.value && event.packet instanceof KeepAliveC2SPacket) {
            packets.add(event.packet);
            event.setCanceled(true);
        }
        if (resourcesPackets.value && event.packet instanceof ResourcePackStatusC2SPacket) {
            packets.add(event.packet);
            event.setCanceled(true);
        }
        if (event.packet instanceof CommonPongC2SPacket) {
            packets.add(event.packet);
            event.setCanceled(true);
        }
        service.schedule(() -> {
            if (mc.player != null) {
                Packet<?> packet = packets.poll();
                if (packet != null) {
                    //No event pushed for this
                    mc.player.networkHandler.getConnection().send(packet);
                }
            }
        }, (long) delay.value, TimeUnit.MILLISECONDS);
    }
}
