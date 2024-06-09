package dev.heliosclient.event.events.render;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

@Cancelable
public class EntityLabelRenderEvent extends Event {

    public final int i;
    final Entity entity;
    final Text entityName;
    final MatrixStack matrixStack;
    final VertexConsumerProvider vertexConsumerProvider;

    public EntityLabelRenderEvent(Entity entity, Text text, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        this.entity = entity;
        this.entityName = text;
        this.matrixStack = matrixStack;
        this.vertexConsumerProvider = vertexConsumerProvider;
        this.i = i;
    }

    public Entity getEntity() {
        return entity;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public Text getEntityName() {
        return entityName;
    }

    public VertexConsumerProvider getVertexConsumerProvider() {
        return vertexConsumerProvider;
    }
}
