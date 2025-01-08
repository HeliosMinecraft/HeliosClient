package dev.heliosclient.util.animation.toasts;


import com.mojang.blaze3d.systems.RenderSystem;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.List;

public class ErrorToast implements net.minecraft.client.toast.Toast {

    private static final Identifier TEXTURE = Identifier.ofVanilla("toast/system");
    private final String message;
    private final boolean hasProgressBar;
    private final long endDelay;
    private long lastTime, startTime;
    private float lastProgress;
    private float progress;


    public ErrorToast(String message, boolean hasProgressBar, long endDelay) {
        this.message = message;
        this.hasProgressBar = hasProgressBar;
        this.endDelay = endDelay;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    @Override
    public Visibility getVisibility() {
        return startTime >= this.endDelay ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    @Override
    public void update(ToastManager manager, long time) {
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
        this.startTime = startTime;
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        context.drawGuiTexture(RenderLayer::getEntityCutoutNoCull,TEXTURE, 0, 0, this.getWidth(), this.getHeight());

        List<String> messageWarp = Renderer2D.wrapText(this.message, this.getWidth() - 5, HeliosClient.MC.textRenderer);

        int yOffset = 6;
        for (String s : messageWarp) {
            context.drawText(textRenderer, s, this.getWidth() - HeliosClient.MC.textRenderer.getWidth(s), yOffset, Color.RED.getRGB(), false);
            yOffset += HeliosClient.MC.textRenderer.fontHeight + 4;
        }

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


            // Calculate the progress based on the delay
            float progress = Math.min(1.0F, (float) startTime / this.endDelay);
            int progressBarWidth = (int) (154.0F * progress);

            // Draw the progress bar
            context.fill(3, 28, 3 + progressBarWidth, 29, Color.RED.getRGB());
        }
    }
}