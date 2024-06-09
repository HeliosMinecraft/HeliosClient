package dev.heliosclient.util.animation.toasts;


import com.mojang.blaze3d.systems.RenderSystem;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.List;

public class InfoToast implements Toast {

    private static final Identifier TEXTURE = new Identifier("textures/gui/sprites/toast/advancement.png");
    private final String message;
    private final boolean hasProgressBar;
    private final long endDelay;
    private long lastTime;
    private float lastProgress;
    private float progress;


    public InfoToast(String message, boolean hasProgressBar, long endDelay) {
        this.message = message;
        this.hasProgressBar = hasProgressBar;
        this.endDelay = endDelay;
    }

    public Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        context.drawTexture(TEXTURE, 0, 0, 0, 0, getWidth(), getHeight());
        List<String> messageWarp = Renderer2D.wrapText(this.message, this.getWidth(), HeliosClient.MC.textRenderer);

        int yOffset = 2;
        for (String s : messageWarp) {
            context.drawText(manager.getClient().textRenderer, s, this.getWidth() - HeliosClient.MC.textRenderer.getWidth(s), yOffset, Color.GREEN.getRGB(), false);
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
            context.fill(3, 28, 3 + progressBarWidth, 29, Color.GREEN.getRGB());
        }

        return startTime >= this.endDelay ? Visibility.HIDE : Visibility.SHOW;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

}