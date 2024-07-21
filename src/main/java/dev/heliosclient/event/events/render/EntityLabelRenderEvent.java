package dev.heliosclient.event.events.render;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

@Cancelable
public class EntityLabelRenderEvent extends Event {

    public static EntityLabelRenderEvent INSTANCE = new EntityLabelRenderEvent();
    public int i;
    public Entity entity;
    public Text entityName;
    public MatrixStack matrixStack;
    public VertexConsumerProvider vertexConsumerProvider;


    public static EntityLabelRenderEvent get(Entity entity, Text text, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        INSTANCE.entity = entity;
        INSTANCE.entityName = text;
        INSTANCE.matrixStack = matrixStack;
        INSTANCE.vertexConsumerProvider = vertexConsumerProvider;
        INSTANCE.i = i;
        return INSTANCE;
    }

    private EntityLabelRenderEvent(){

    }


    public Entity getEntity() {
        return INSTANCE.entity;
    }

    public MatrixStack getMatrixStack() {
        return INSTANCE.matrixStack;
    }

    public Text getEntityName() {
        return INSTANCE.entityName;
    }

    public VertexConsumerProvider getVertexConsumerProvider() {
        return INSTANCE.vertexConsumerProvider;
    }
}
