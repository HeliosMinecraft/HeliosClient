package dev.heliosclient.event.events.client;

import dev.heliosclient.event.Event;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

public class ServerConnectHeadEvent extends Event {
    public final ServerInfo info;
    public final ServerAddress address;

    public ServerConnectHeadEvent(ServerInfo info, ServerAddress address) {
        this.info = info;
        this.address = address;
    }
}
