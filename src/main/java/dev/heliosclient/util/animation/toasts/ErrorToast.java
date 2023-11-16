package dev.heliosclient.util.animation.toasts;


import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.AdvancementToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

public class ErrorToast implements net.minecraft.client.toast.Toast {

    private final String message;
    private final boolean hasProgressBar;
    private long lastTime;
    private final long endDelay;
    private float lastProgress;
    private float progress;
    private static final Identifier TEXTURE = new Identifier("toast/system");


    public ErrorToast(String message, boolean hasProgressBar, long endDelay) {
        this.message = message;
        this.hasProgressBar = hasProgressBar;
        this.endDelay = endDelay;
    }

    public Toast.Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        context.drawTexture(TEXTURE, 0, 0, 0, 96, this.getWidth(), this.getHeight());
        context.drawText(manager.getClient().textRenderer, this.message, 30, 12, -11534256, false);

        if (this.hasProgressBar) {
            context.fill(3, 28, 157, 29, -1);
            float f = MathHelper.clampedLerp(this.lastProgress, this.progress, (float) (startTime - this.lastTime) / 100.0F);
            int i;
            if (this.progress >= this.lastProgress) {
                i = -16755456;
            } else {
                i = -11206656;
            }

            context.fill(3, 28, (int) (3.0F + 154.0F * f), 29, i);
            this.lastProgress = f;
            this.lastTime = startTime;
        }

        // Calculate the progress based on the delay
        float progress = Math.min(1.0F, (float) startTime / this.endDelay);
        int progressBarWidth = (int) (154.0F * progress);

        // Draw the progress bar
        context.fill(3, 28, 3 + progressBarWidth, 29, Color.RED.getRGB());

        return startTime >= this.endDelay ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

}