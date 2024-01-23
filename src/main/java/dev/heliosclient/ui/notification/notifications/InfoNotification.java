package dev.heliosclient.ui.notification.notifications;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.ui.notification.Notification;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.SoundUtils;
import dev.heliosclient.util.fontutils.fxFontRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvent;

public class InfoNotification extends Notification {
    private final String title;
    private final String description;

    public InfoNotification(String title, String description, long endDelay, SoundEvent soundEvent, float pitch) {
        this.title = title;
        this.description = description;
        this.endDelay = endDelay;
        this.WIDTH = 50;
        if (ModuleManager.notificationModule.playSound.value && ModuleManager.notificationModule.isActive()) {
            SoundUtils.playSound(soundEvent, (float) (ModuleManager.notificationModule.volume.value / 100f), pitch);
        }
    }

    @Override
    public void render(MatrixStack matrices, int y, fxFontRenderer fontRenderer) {
        this.targetY = y;

        Renderer2D.drawRoundedRectangle(matrices.peek().getPositionMatrix(), x, y, true, true, false, false, WIDTH, HEIGHT, 3, ColorManager.INSTANCE.clickGuiPrimary);

        int titleHeight = Math.round(Math.max((fontRenderer.getStringWidth(title) / 2), (fontRenderer.getStringWidth(description) / 2)));
        if (this.WIDTH <= fontRenderer.getStringWidth(description)) {
            this.WIDTH = Math.round(fontRenderer.getStringWidth(description) + 3);
        } else if (this.WIDTH <= fontRenderer.getStringWidth(title)) {
            this.WIDTH = Math.round(fontRenderer.getStringWidth(title) + 3);
        }

        fontRenderer.drawString(matrices, title, x + (((float) WIDTH / 2) - titleHeight) + 2, y + 1 + fontRenderer.getStringHeight(title) / 2, -1);
        fontRenderer.drawString(matrices, description, x + (((float) WIDTH / 2) - (fontRenderer.getStringWidth(description) / 2)), y + 9 + fontRenderer.getStringHeight(description) / 2, -1);

        // Draw progress bar
        float progress = Math.min(timeElapsed / (float) endDelay, 1);
        Renderer2D.drawRectangle(matrices.peek().getPositionMatrix(), x, this.y + HEIGHT - 1, (int) (WIDTH * progress), 1, HeliosClient.uiColor);
    }
}