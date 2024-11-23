package dev.heliosclient.mixin;

import dev.heliosclient.event.events.input.KeyboardInputEvent;
import dev.heliosclient.managers.EventManager;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class MixinKeyboardInput extends Input {
    @Shadow
    private static float getMovementMultiplier(boolean positive, boolean negative) {
        return 0;
    }

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/input/KeyboardInput;sneaking:Z", shift = At.Shift.AFTER), allow = 1)
    private void onTick(boolean slowDown, float slowDownFactor, CallbackInfo ci) {
        KeyboardInputEvent event = new KeyboardInputEvent(pressingForward, pressingBack, pressingLeft, pressingRight, jumping, sneaking);
        EventManager.postEvent(event);

        if(event.isCanceled()){
            this.pressingForward = event.pressingForward;
            this.pressingBack = event.pressingBack;
            this.pressingLeft = event.pressingLeft;
            this.pressingRight = event.pressingRight;

            this.movementForward = getMovementMultiplier(this.pressingForward, this.pressingBack);
            this.movementSideways = getMovementMultiplier(this.pressingLeft, this.pressingRight);

            if (event.shouldApplyMovementForward()) {
                this.movementForward = event.movementForward;
            }
            if (event.shouldApplyMovementSideways()) {
                this.movementSideways = event.movementSideways;
            }

            this.jumping = event.jumping;
            this.sneaking = event.sneaking;
        }
    }
}
