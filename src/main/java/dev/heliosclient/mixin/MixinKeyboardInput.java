package dev.heliosclient.mixin;

import dev.heliosclient.event.events.input.KeyboardInputEvent;
import dev.heliosclient.managers.EventManager;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Final;
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

    @Shadow @Final private GameOptions settings;

    @Inject(method = "tick", at = @At(value = "HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        KeyboardInputEvent event = new KeyboardInputEvent(this.settings.forwardKey.isPressed(), this.settings.backKey.isPressed(), this.settings.leftKey.isPressed(), this.settings.rightKey.isPressed(), this.settings.jumpKey.isPressed(), this.settings.sneakKey.isPressed(), this.settings.sprintKey.isPressed());
        EventManager.postEvent(event);

        if(event.isCanceled()){
            this.playerInput = new PlayerInput(event.pressingForward,event.pressingBack,event.pressingLeft,event.pressingRight,event.jumping,event.sneaking,event.sprinting);

            this.movementForward = getMovementMultiplier(playerInput.forward(),playerInput.backward());
            this.movementSideways = getMovementMultiplier(playerInput.left(), playerInput.right());

            if (event.shouldApplyMovementForward()) {
                this.movementForward = event.movementForward;
            }
            if (event.shouldApplyMovementSideways()) {
                this.movementSideways = event.movementSideways;
            }
            ci.cancel();
        }
    }
}
