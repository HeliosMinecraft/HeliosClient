package dev.heliosclient.mixin;

import dev.heliosclient.system.mixininterface.IEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public abstract class MixinEntityRenderState implements IEntityRenderState {

    @Unique
    private Entity entity;

    @Override
    public Entity helios$getEntity() {
        return entity;
    }

    @Override
    public void helios$setEntity(Entity entity) {
        this.entity = entity;
    }
}
