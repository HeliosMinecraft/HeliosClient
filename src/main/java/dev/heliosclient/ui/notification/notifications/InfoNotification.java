package dev.heliosclient.ui.notification.notifications;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.misc.NotificationModule;
import dev.heliosclient.ui.notification.Notification;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.SoundUtils;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.fontutils.fxFontRenderer;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvent;

import java.awt.*;

public class InfoNotification extends Notification {
    private final String title;
    private final String description;

    public InfoNotification(String title, String description, long endDelay, SoundEvent soundEvent, float pitch) {
        this.soundEvent = soundEvent;
        this.pitch = pitch;
        this.title = title;
        this.description = description;
        this.endDelay = endDelay;
        this.endDelayInS = endDelay / 1000.0f;
        this.WIDTH = 63;
        initialise();
    }

    public InfoNotification(String title, String description, long endDelay, SoundEvent soundEvent) {
        this(title, description, endDelay, soundEvent, 1.0f);
    }

    @Override
    public void playSound(SoundEvent soundEvent, float volume, float pitch) {
        if (ModuleManager.get(NotificationModule.class).playSound.value && ModuleManager.get(NotificationModule.class).isActive()) {
            SoundUtils.playSound(soundEvent, (float) (ModuleManager.get(NotificationModule.class).volume.value / 100f), pitch);
        }
    }

    @Override
    public void render(MatrixStack matrices, int y, fxFontRenderer fontRenderer) {
        this.targetY = y;
        String titleText = title;
        if (title.length() > 11) {
            titleText = title.substring(0, 11) + "..";
        }

        Renderer2D.drawRoundedRectangle(matrices.peek().getPositionMatrix(), x, y, true, true, false, false, WIDTH, HEIGHT, 3, ColorManager.INSTANCE.clickGuiPrimary);

        fontRenderer.drawString(matrices, titleText, x + 17, y + 1 + fontRenderer.getStringHeight(title) / 2, -1);
        fontRenderer.drawString(matrices, description, x + 17, y + 9 + fontRenderer.getStringHeight(description) / 2, -1);

        Renderer2D.drawFilledCircle(matrices.peek().getPositionMatrix(), x + 9, y + 12, 4.0f, ColorUtils.changeAlpha(Color.YELLOW, 120).getRGB());
        Renderer2D.drawCircle(matrices.peek().getPositionMatrix(), x + 9, y + 12, 4.0f, 0.33f, ColorUtils.changeAlpha(Color.WHITE, 120).getRGB());

        FontRenderers.Small_iconRenderer.drawString(matrices, "\uEAC3", x + 6f, y + 9f, Color.WHITE.getRGB());

        // Draw progress bar
        float progress = Math.min(timeElapsed / (float) endDelay, 1);
        Renderer2D.drawRectangle(matrices.peek().getPositionMatrix(), x, this.y + HEIGHT - 1, (int) (WIDTH * progress), 1, ColorManager.INSTANCE.getPrimaryGradientStart().getRGB());
    }
}