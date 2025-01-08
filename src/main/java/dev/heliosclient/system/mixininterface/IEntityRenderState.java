package dev.heliosclient.system.mixininterface;

import net.minecraft.entity.Entity;

public interface IEntityRenderState {
    void helios$setEntity(Entity entity);
    Entity helios$getEntity();
}
