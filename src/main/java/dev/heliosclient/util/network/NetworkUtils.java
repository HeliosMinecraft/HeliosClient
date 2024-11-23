package dev.heliosclient.util.network;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.mixin.AccessorClientWorld;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.network.packet.Packet;

public class NetworkUtils {
    public static void sendSequencedPacket(SequencedPacketCreator creator) {
        PendingUpdateManager pendingUpdateManager = ((AccessorClientWorld) HeliosClient.MC.world).getPendingUpdateManager();
        pendingUpdateManager.incrementSequence();

        Packet<?> packet = creator.predict(pendingUpdateManager.getSequence());

        HeliosClient.MC.getNetworkHandler().sendPacket(packet);
    }
    public static void sendPacketNoEvent(Packet<?> packet) {
        HeliosClient.MC.getNetworkHandler().getConnection().send(packet);
    }
}
