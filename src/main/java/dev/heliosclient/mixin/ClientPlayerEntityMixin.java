package dev.heliosclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.player.PlayerMotionEvent;
import dev.heliosclient.event.events.player.PostMovementUpdatePlayerEvent;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.movement.NoSlow;
import dev.heliosclient.module.modules.movement.Velocity;
import dev.heliosclient.module.modules.render.Freecam;
import dev.heliosclient.module.modules.world.BetterPortals;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientPlayerEntity.class, priority = 600)
public abstract class ClientPlayerEntityMixin {

    @Unique
    public boolean doNotTickSelf = false;

    @Shadow
    public abstract void tick();

    @Shadow
    protected abstract void sendMovementPackets();

    @Inject(method = "move", at = @At(value = "HEAD"), cancellable = true)
    public void onMove(MovementType type, Vec3d movement, CallbackInfo ci) {
        PlayerMotionEvent event = new PlayerMotionEvent(type, movement);
        EventManager.postEvent(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "isCamera", at = @At("HEAD"), cancellable = true)
    private void onIsCamera(CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.get(Freecam.class).isActive() && this.equals(MinecraftClient.getInstance().player)) {
            cir.setReturnValue(true);
        }
    }


    @Inject(method = "init", at = @At(value = "TAIL"))
    public void onInit(CallbackInfo ci) {
        for (Module_ m : ModuleManager.getEnabledModules()) {
            m.onEnable();
        }
    }

    @ModifyExpressionValue(method = "canSprint", at = @At(value = "CONSTANT", args = "floatValue=6.0f"))
    private float onHunger(float constant) {
        if (NoSlow.get().hunger.value && NoSlow.get().isActive()) return -1;
        return constant;
    }

    @Inject(method = "tick", at = @At(value = "HEAD"), cancellable = true)
    public void onTick(CallbackInfo ci) {
        if (HeliosClient.MC.player != null) {
            if (EventManager.postEvent(TickEvent.PLAYER.get(HeliosClient.MC.player)).isCanceled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void noPush(CallbackInfo callbackInfo) {
        if (ModuleManager.get(Velocity.class).isActive() && ModuleManager.get(Velocity.class).noPush.value) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;sendMovementPackets()V", shift = At.Shift.BEFORE), cancellable = true)
    private void PostUpdateHook(CallbackInfo info) {
        if (doNotTickSelf) {
            return;
        }
        PostMovementUpdatePlayerEvent event = new PostMovementUpdatePlayerEvent();
        EventManager.postEvent(event);
        if (event.isCanceled()) {
            info.cancel();
            if (event.numberOfTicks > 0) {
                for (int i = 0; i < event.numberOfTicks; i++) {
                    doNotTickSelf = true;
                    tick();
                    doNotTickSelf = false;
                    sendMovementPackets();
                }
            }
            event.shiftedTicks = true;
        }
    }

    @Redirect(method = "updateNausea", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;"))
    private Screen updateNausea$setcurrentScreen(MinecraftClient client) {
        if (ModuleManager.get(BetterPortals.class).isActive()) return null;
        return client.currentScreen;
    }
}
