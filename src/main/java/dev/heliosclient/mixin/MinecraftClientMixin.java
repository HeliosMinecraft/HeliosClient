package dev.heliosclient.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.client.ClientStopEvent;
import dev.heliosclient.event.events.client.OpenScreenEvent;
import dev.heliosclient.event.events.player.DisconnectEvent;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.player.FastUse;
import dev.heliosclient.module.modules.render.ESP;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SystemDetails;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    public ClientWorld world;

    @Shadow
    private int itemUseCooldown;

    @Shadow @Final private Window window;

    @Shadow public abstract Window getWindow();

    @Inject(method = "tick", at = @At(value = "HEAD"), cancellable = true)
    public void onTick(CallbackInfo ci) {
        TickEvent clientTick = new TickEvent.CLIENT();
        EventManager.postEvent(clientTick);

        if (clientTick.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "addSystemDetailsToCrashReport(Lnet/minecraft/util/SystemDetails;Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/resource/language/LanguageManager;Ljava/lang/String;Lnet/minecraft/client/option/GameOptions;)Lnet/minecraft/util/SystemDetails;", at = @At(value = "RETURN"))
    private static void onAddSystemDetails(SystemDetails systemDetails, MinecraftClient client, LanguageManager languageManager, String version, GameOptions options, CallbackInfoReturnable<SystemDetails> cir) {
        if(systemDetails != null) {
            systemDetails.addSection("HeliosClient", () -> "Version " + HeliosClient.versionTag + ", \n            Active Modules:" + ModuleManager.getEnabledModules());
        }
    }

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemEnabled(Lnet/minecraft/resource/featuretoggle/FeatureSet;)Z"))
    private void onItemUse(CallbackInfo ci, @Local ItemStack itemStack) {
        if (Objects.requireNonNull(ModuleManager.get(FastUse.class)).isActive()) {
            itemUseCooldown = ModuleManager.get(FastUse.class).getCoolDown(itemStack);
        }
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("TAIL"), cancellable = true)
    private void onDisconnect(Screen screen, CallbackInfo info) {
        if (world != null) {
            PlayerEntity player = HeliosClient.MC.player;
            Event event = new DisconnectEvent(player);
            if (EventManager.postEvent(event).isCanceled())
                info.cancel();
        }
    }

    @Inject(method = "stop", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;)V", shift = At.Shift.AFTER, remap = false))
    private void onStopping(CallbackInfo ci) {
        EventManager.postEvent(new ClientStopEvent());
    }

    @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
    private void onHasOutline(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        ESP esp = ModuleManager.get(ESP.class);
        if (esp.isActive() && esp.boxMode.getOption() == ESP.BoxMode.EntityOutline && !esp.isBlackListed(entity)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (EventManager.postEvent(new OpenScreenEvent(screen)).isCanceled()) {
            ci.cancel();
        }
    }
}
