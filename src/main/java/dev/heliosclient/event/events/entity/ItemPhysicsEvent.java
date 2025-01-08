package dev.heliosclient.event.events.entity;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;

@Cancelable
public class ItemPhysicsEvent extends Event {
    public final ItemEntityRenderState itemEntityRenderState;
    public final int i;
    public final MatrixStack matrixStack;
    public final VertexConsumerProvider vertexConsumerProvider;


    public ItemPhysicsEvent(ItemEntityRenderState itemEntityRenderState, int i, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider) {
        this.itemEntityRenderState = itemEntityRenderState;
        this.i = i;
        this.matrixStack = matrixStack;
        this.vertexConsumerProvider = vertexConsumerProvider;
    }
}
