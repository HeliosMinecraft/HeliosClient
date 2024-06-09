package dev.heliosclient.event.events.entity;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;

@Cancelable
public class ItemPhysicsEvent extends Event {
    public final ItemEntity item;
    public final float f, g;
    public final int i;
    public final ItemRenderer itemRenderer;
    public final MatrixStack matrixStack;
    public final VertexConsumerProvider vertexConsumerProvider;


    public ItemPhysicsEvent(ItemEntity item, float f, float g, int i, ItemRenderer itemRenderer, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider) {
        this.item = item;
        this.f = f;
        this.g = g;
        this.i = i;
        this.itemRenderer = itemRenderer;
        this.matrixStack = matrixStack;
        this.vertexConsumerProvider = vertexConsumerProvider;
    }

    public ItemEntity getItem() {
        return item;
    }
}
