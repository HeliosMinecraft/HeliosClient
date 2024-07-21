package dev.heliosclient.system.mixininterface;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

public interface IPlayerInteractEntityC2SPacket {
    @SuppressWarnings("all")
    PlayerInteractEntityC2SPacket.InteractType getType();

    Entity getEntity();
}