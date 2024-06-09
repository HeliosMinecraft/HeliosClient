package dev.heliosclient.mixin;

import dev.heliosclient.event.events.entity.EntityAddedEvent;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.module.modules.render.NoRender;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ClientWorld.class)
public class MixinClientWorld {

    @Inject(at = @At("HEAD"), method = "addEntity")
    private void onAddEntity(Entity entity, CallbackInfo ci) {
        if (entity != null)
            EventManager.postEvent(new EntityAddedEvent(entity));
    }

    @ModifyArgs(method = "doRandomBlockDisplayTicks", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;randomBlockDisplayTick(IIIILnet/minecraft/util/math/random/Random;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos$Mutable;)V"))
    private void doRandomBlockDisplayTicks(Args args) {
        if (NoRender.get().isActive() && NoRender.get().noInvisible.value) {
            args.set(5, Blocks.BARRIER);
        }
    }
}
