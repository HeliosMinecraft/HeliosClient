package dev.heliosclient.event.events.render;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;

@Cancelable
public class ArmRenderEvent extends Event {
    private final Hand hand;
    private final MatrixStack matrix;

    public ArmRenderEvent(Hand hand, MatrixStack stack) {
        this.hand = hand;
        this.matrix = stack;
    }

    public Hand getHand() {
        return hand;
    }

    public MatrixStack getMatrix() {
        return matrix;
    }
}
