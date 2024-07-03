package dev.heliosclient.mixin;

import com.mojang.brigadier.ParseResults;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.events.player.PlayerLeaveEvent;
import dev.heliosclient.managers.EventManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler {
    @Shadow
    protected abstract ParseResults<ServerCommandSource> parse(String command);

    @Inject(method = "onDisconnected", at = @At("RETURN"), cancellable = true)
    private void onDisconnected(Text reason, CallbackInfo ci) {
        PlayerLeaveEvent event = new PlayerLeaveEvent(HeliosClient.MC.player);
        EventManager.postEvent(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
