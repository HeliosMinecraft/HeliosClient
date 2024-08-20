package dev.heliosclient.mixin;

import dev.heliosclient.event.events.block.*;
import dev.heliosclient.event.events.player.PlayerAttackEntityEvent;
import dev.heliosclient.event.events.player.ReachEvent;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.player.NoBreakDelay;
import dev.heliosclient.module.modules.render.Freecam;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager {

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    public abstract float getReachDistance();

    @Shadow public abstract boolean breakBlock(BlockPos pos);

    @Shadow @Final private ClientPlayNetworkHandler networkHandler;

    @Inject(method = "getReachDistance()F", at = @At(value = "HEAD"), cancellable = true)
    private void getReach(CallbackInfoReturnable<Float> cir) {
        ReachEvent reachEvent = new ReachEvent(cir.getReturnValueF());
        EventManager.postEvent(reachEvent);
        if (reachEvent.isCanceled())
            cir.setReturnValue(reachEvent.getReach());
    }

    @Inject(method = "hasExtendedReach()Z", at = @At(value = "HEAD"), cancellable = true)
    private void onHasExtendedReach(CallbackInfoReturnable<Boolean> cir) {
        ReachEvent reachEvent = new ReachEvent(getReachDistance());
        EventManager.postEvent(reachEvent);
        if (reachEvent.isCanceled()) cir.setReturnValue(true);
    }

    @Inject(method = "attackEntity", at = @At(value = "TAIL"), cancellable = true)
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        PlayerAttackEntityEvent event = new PlayerAttackEntityEvent(player, target);
        if (EventManager.postEvent(event).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "attackEntity", at = @At(value = "HEAD"), cancellable = true)
    private void onAttackEntityPRE(PlayerEntity player, Entity target, CallbackInfo ci) {
        Freecam freecam = ModuleManager.get(Freecam.class);
        if (freecam.isActive() && target == client.player) {
            ci.cancel();
        }

        PlayerAttackEntityEvent.PRE event = new PlayerAttackEntityEvent.PRE(player, target);
        if (EventManager.postEvent(event).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "attackBlock", at = @At(value = "HEAD"), cancellable = true)
    private void onAttackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        BeginBreakingBlockEvent event = new BeginBreakingBlockEvent(pos, direction);
        if (EventManager.postEvent(event).isCanceled()) {
            cir.cancel();
        }
    }

    @Inject(method = "attackBlock", at = @At(value = "RETURN"), cancellable = true)
    private void onAttackBlockPost(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        PostAttackBlockEvent event = new PostAttackBlockEvent(pos, direction);
        if (EventManager.postEvent(event).isCanceled()) {
            cir.cancel();
        }
    }

    @Inject(method = "interactBlock", at = @At(value = "HEAD"), cancellable = true)
    private void onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        BlockInteractEvent event = new BlockInteractEvent(hitResult, hand);
        if (EventManager.postEvent(event).isCanceled()) {
            cir.setReturnValue(ActionResult.PASS);
            cir.cancel();
        }
    }

    @Inject(method = "breakBlock", at = @At(value = "HEAD"), cancellable = true)
    private void onBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = client.player.getWorld().getBlockState(pos);
        BlockBreakEvent event = new BlockBreakEvent(pos, state);
        EventManager.postEvent(event);
        if (event.isCanceled()) {
            cir.setReturnValue(event.isCanceled());
        }
    }

    @ModifyConstant(method = "updateBlockBreakingProgress", constant = @Constant(intValue = 5))
    private int updateBlockBreakingProgress(int value) {
        NoBreakDelay nbd = ModuleManager.get(NoBreakDelay.class);
        if (nbd.isActive()) {
            return (int) nbd.breakDelay.value;
        }

        return value;
    }

    @Inject(method = "cancelBlockBreaking", at = @At("HEAD"), cancellable = true)
    private void hookCancelBlockBreaking(CallbackInfo callbackInfo) {
        if (EventManager.postEvent(new CancelBlockBreakingEvent()).isCanceled()) {
            callbackInfo.cancel();
        }
    }

}
