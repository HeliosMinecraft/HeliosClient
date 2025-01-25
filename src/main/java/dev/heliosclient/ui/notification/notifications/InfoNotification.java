package dev.heliosclient.ui.notification.notifications;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.misc.NotificationModule;
import dev.heliosclient.ui.notification.Notification;
import dev.heliosclient.util.SoundUtils;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.fontutils.BetterFontRenderer;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvent;

import java.awt.*;

public class InfoNotification extends Notification {
    private final String title;
    private final String description;

    public InfoNotification(String title, String description) {
        this(title, description, 5000);
    }

    public InfoNotification(String title, String description, long endDelay) {
        this(title, description, endDelay, null);
    }

    public InfoNotification(String title, String description, long endDelay, SoundEvent soundEvent) {
        this(title, description, endDelay, soundEvent, 1.0f);
    }

    public InfoNotification(String title, String description, long endDelay, SoundEvent soundEvent, float pitch) {
        super(63, 25);

        this.title = title;
        this.description = description;
        this.soundEvent = soundEvent;
        this.pitch = pitch;
        this.endDelay = endDelay;
    }

    @Override
    @SuppressWarnings("all")
    public void playSound(SoundEvent soundEvent, float volume, float pitch) {
        if (ModuleManager.get(NotificationModule.class).playSound.value &&
                ModuleManager.get(NotificationModule.class).isActive()) {
            SoundUtils.playSound(
                    soundEvent,
                    (float) (ModuleManager.get(NotificationModule.class).volume.value / 100f),
                    pitch
            );
        }
    }

    @Override
    public void update() {
        super.update();
        // Ensure default dimensions if not in fancy mode
        if (IS_FANCY) {
            int newWidth = Math.round(FontRenderers.Small_fxfontRenderer.getStringWidth(title + " " + description) + 6);

            // Only recalculate if width has actually changed
            if (newWidth != width) {
                updateDimensions(newWidth, 18);
            }
        } else if (width != 63 || height != 25) {
            updateDimensions(63, 25);
        }
    }

    @Override
    public void render(MatrixStack matrices, int y, BetterFontRenderer fontRenderer) {
        if (IS_FANCY) {
            renderFancyStyle(matrices, fontRenderer);
            return;
        }

        renderDefaultStyle(matrices, fontRenderer);
    }

    private void renderFancyStyle(MatrixStack matrices, BetterFontRenderer fontRenderer) {
        //Fancy, but neither customisable nor practical.
        //TODO : ^^^

        // Gradient background
        Renderer2D.drawRoundedGradientRectangle(
                matrices.peek().getPositionMatrix(),
                ColorManager.INSTANCE.getPrimaryGradientStart(),
                ColorManager.INSTANCE.getPrimaryGradientEnd(),
                ColorManager.INSTANCE.getPrimaryGradientEnd(),
                ColorManager.INSTANCE.getPrimaryGradientStart(),
                x, y, width, height, 3
        );

        // Centered text
        fontRenderer.drawString(
                matrices,
                title + " " + description,
                x + 3,
                y + height / 2.0f - fontRenderer.getStringHeight(Renderer2D.TEXT) / 2.0f,
                -1
        );
    }

    private void renderDefaultStyle(MatrixStack matrices, BetterFontRenderer fontRenderer) {
        String titleText = fontRenderer.trimToWidth(title, width - 17);

        // Rounded rectangle background
        Renderer2D.drawRoundedRectangle(
                matrices.peek().getPositionMatrix(),
                x, y, true, true, false, false,
                width, height, 3,
                ColorManager.INSTANCE.clickGuiPrimary
        );

        // Title and description text
        fontRenderer.drawString(matrices, titleText, x + 17, y + 1 + fontRenderer.getStringHeight(title) / 2, -1);
        fontRenderer.drawString(matrices, description, x + 17, y + 9 + fontRenderer.getStringHeight(description) / 2, -1);

        // Circular icon background and outline
        Renderer2D.drawFilledCircle(
                matrices.peek().getPositionMatrix(),
                x + 9, y + 12, 4.0f,
                ColorUtils.changeAlpha(Color.YELLOW, 120).getRGB()
        );
        Renderer2D.drawCircle(
                matrices.peek().getPositionMatrix(),
                x + 9, y + 12, 4.0f, 0.33f,
                ColorUtils.changeAlpha(Color.WHITE, 120).getRGB()
        );

        // Icon
        FontRenderers.Small_iconRenderer.drawString(
                matrices, "\uEAC3",
                x + 6f, y + 9f,
                Color.WHITE.getRGB()
        );

        // Progress bar
        float progress = Math.min(timeElapsed / (float) endDelay, 1);
        Renderer2D.drawRectangle(
                matrices.peek().getPositionMatrix(),
                x, y + height - 1,
                (int) (width * progress), 1,
                ColorManager.INSTANCE.getPrimaryGradientStart().getRGB()
        );
    }
}