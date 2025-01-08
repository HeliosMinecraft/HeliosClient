package dev.heliosclient.mixin;

import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.entity.EntityAddedEvent;
import dev.heliosclient.event.events.entity.EntityRemovedEvent;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.render.NoRender;
import dev.heliosclient.module.modules.render.TimeChanger;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld {

    @Shadow @Nullable public abstract Entity getEntityById(int id);

    @Inject(at = @At("HEAD"), method = "addEntity")
    private void onAddEntity(Entity entity, CallbackInfo ci) {
        if (entity != null)
            EventManager.postEvent(new EntityAddedEvent(entity));
    }
    @Inject(at = @At("HEAD"), method = "removeEntity")
    private void onRemoveEntity(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci) {
        if (getEntityById(entityId) != null)
            EventManager.postEvent(new EntityRemovedEvent(getEntityById(entityId),removalReason,entityId));
    }


    @Inject(method = "tickEntities", at = @At("HEAD"), cancellable = true)
    private void tickEntities(CallbackInfo info) {
        TickEvent.WORLD event = new TickEvent.WORLD();
        if (EventManager.postEvent(event).isCanceled())
            info.cancel();
    }

    @ModifyVariable(method = "setTime", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private long modifySetTimeOfDay(long timeOfDay) {
        TimeChanger timeChanger = ModuleManager.get(TimeChanger.class);
        if (timeChanger.isActive()) {
            return (long) timeChanger.time.value;
        }
        return timeOfDay;
    }

    @ModifyArgs(method = "doRandomBlockDisplayTicks", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;randomBlockDisplayTick(IIIILnet/minecraft/util/math/random/Random;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos$Mutable;)V"))
    private void doRandomBlockDisplayTicks(Args args) {
        if (NoRender.get().isActive() && NoRender.get().trueSight.value) {
            args.set(5, Blocks.BARRIER);
        }
    }
}
