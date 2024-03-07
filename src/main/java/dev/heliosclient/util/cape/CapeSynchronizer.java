package dev.heliosclient.util.cape;

import dev.heliosclient.managers.CapeManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.UUID;


@Deprecated
public class CapeSynchronizer {
    private static final Identifier CAPE_SYNC_PACKET_ID = new Identifier("heliosclient", "cape_sync");

    public static void sendCapeSyncPacket(ServerPlayerEntity player, Identifier capeTexture, Identifier identifier) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeUuid(player.getUuid());
        buf.writeIdentifier(capeTexture);
        buf.writeIdentifier(identifier);

        ServerPlayNetworking.send(player, CAPE_SYNC_PACKET_ID, buf);
    }

    public static void registerCapeSyncPacket() {
        ServerPlayNetworking.registerGlobalReceiver(CAPE_SYNC_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            Identifier capeTexture = buf.readIdentifier();
            Identifier elytraIdentifier = buf.readIdentifier();


///            server.execute(() -> CapeManager.set(Objects.requireNonNull(server.getPlayerManager().getPlayer(uuid)), capeTexture, elytraIdentifier));
        });
    }
}
