package dev.heliosclient.mixin;

import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.world.Timer;
import net.minecraft.client.render.RenderTickCounter;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.Dynamic.class)
public abstract class MixinRenderTickCounter {
    @Shadow
    private float lastFrameDuration;

    @Shadow private long prevTimeMillis;

    @Shadow @Final private float tickTime;

    @Inject(method = "beginRenderTick(J)I", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter$Dynamic;prevTimeMillis:J", opcode = Opcodes.PUTFIELD))
    private void onBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        this.lastFrameDuration = (float) ((float)(timeMillis - this.prevTimeMillis) / tickTime * ModuleManager.get(Timer.class).getTimerMultiplier());
    }
}
