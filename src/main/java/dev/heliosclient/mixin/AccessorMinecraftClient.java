package dev.heliosclient.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.net.Proxy;

@Mixin(MinecraftClient.class)
public interface AccessorMinecraftClient {
    @Mutable
    @Accessor("session")
    void setSession(Session session);

    @Invoker("doAttack")
    boolean leftClick();

    @Invoker("doItemUse")
    void rightClick();

    @Accessor("networkProxy")
    Proxy getProxy();
}
