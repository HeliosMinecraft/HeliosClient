package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.client.OpenScreenEvent;
import dev.heliosclient.event.events.player.PlayerLeaveEvent;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.player.FastUse;
import dev.heliosclient.module.modules.render.ESP;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Objects;

import static net.minecraft.client.MinecraftClient.getInstance;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    public ClientWorld world;

    @Shadow
    private int itemUseCooldown;

    @Inject(method = "tick", at = @At(value = "HEAD"), cancellable = true)
    public void onTick(CallbackInfo ci) {
        TickEvent ClientTick = new TickEvent.CLIENT(getInstance());
        EventManager.postEvent(ClientTick);

        TickEvent DefaultTick = new TickEvent();
        EventManager.postEvent(DefaultTick);

        if (ClientTick.isCanceled() || DefaultTick.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemEnabled(Lnet/minecraft/resource/featuretoggle/FeatureSet;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onItemUse(CallbackInfo ci, Hand[] var1, int var2, int var3, Hand hand, ItemStack itemStack) {
        if (Objects.requireNonNull(ModuleManager.get(FastUse.class)).isActive()) {
            itemUseCooldown = ModuleManager.get(FastUse.class).getCoolDown(itemStack);
        }
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("TAIL"), cancellable = true)
    private void onDisconnect(Screen screen, CallbackInfo info) {
        if (world != null) {
            PlayerEntity player = HeliosClient.MC.player;
            Event event = new PlayerLeaveEvent(player);
            if (EventManager.postEvent(event).isCanceled())
                info.cancel();
        }
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
