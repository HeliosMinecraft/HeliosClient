package dev.heliosclient.event.events.player;

import dev.heliosclient.event.Event;

public class SendMovementPacketEvent extends Event {
    public static class PRE extends SendMovementPacketEvent{}
    public static class POST extends SendMovementPacketEvent{}
}
