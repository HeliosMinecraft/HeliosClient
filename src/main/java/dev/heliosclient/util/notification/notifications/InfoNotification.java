package dev.heliosclient.util.notification.notifications;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.SoundUtils;
import dev.heliosclient.util.fontutils.fxFontRenderer;
import dev.heliosclient.util.notification.Notification;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundCategory;

public class InfoNotification extends Notification {
    private final String title;
    private final String description;

    public InfoNotification(String title, String description, long endDelay) {
        this.title = title;
        this.description = description;
        this.endDelay = endDelay;
        this.WIDTH = 50;
        SoundUtils.playSound(SoundUtils.TING_SOUNDEVENT, HeliosClient.MC.options.getSoundVolume(SoundCategory.MASTER), 100f);
    }

    @Override
    public void render(MatrixStack matrices, int y, fxFontRenderer fontRenderer) {
        this.targetY = y;

        Renderer2D.drawRoundedRectangle(matrices.peek().getPositionMatrix(), currentX, currentY, true, true, false, false, WIDTH, HEIGHT, 3, ColorManager.INSTANCE.clickGuiPrimary);

        fontRenderer.drawString(matrices, title, currentX + ((WIDTH / 2) - (fontRenderer.getStringWidth(description) / 2)) + 2, currentY + 1 + fontRenderer.getStringHeight(title) / 2, -1);
        fontRenderer.drawString(matrices, description, currentX + ((WIDTH / 2) - (fontRenderer.getStringWidth(description) / 2)), currentY + 9 + fontRenderer.getStringHeight(description) / 2, -1);
        // Draw progress bar
        long timeElapsed = System.currentTimeMillis() - creationTime;
        float progress = Math.min(timeElapsed / (float) endDelay, 1);
        Renderer2D.drawRectangle(matrices.peek().getPositionMatrix(), currentX, currentY + HEIGHT - 1, (int) (WIDTH * progress), 1, HeliosClient.uiColor);
    }
}