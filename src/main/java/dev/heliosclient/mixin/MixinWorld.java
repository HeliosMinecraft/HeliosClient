package dev.heliosclient.mixin;


import dev.heliosclient.event.events.world.ExplosionEvent;
import dev.heliosclient.managers.EventManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class MixinWorld {
    @Inject(at = @At("TAIL"), method = "createExplosion*")
    private void onCreateExplosion(CallbackInfoReturnable<Explosion> cir) {
        Explosion explosion = cir.getReturnValue();
        if (explosion != null) {
            LivingEntity entity = explosion.getCausingEntity();
            float power = explosion.getPower();
            EventManager.postEvent(new ExplosionEvent(entity, power));
        }
    }

}
