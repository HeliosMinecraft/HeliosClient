package dev.heliosclient.managers;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.render.RenderEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.module.modules.misc.NotificationModule;
import dev.heliosclient.ui.notification.Notification;
import dev.heliosclient.util.fontutils.FontRenderers;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NotificationManager implements Listener {
    private static final int HEIGHT = 25;
    private static final Queue<Notification> notificationQueue = new LinkedList<>(); // Queue to hold notifications
    private static final Deque<Notification> displayedNotifications = new ArrayDeque<>(); // Deque to hold currently displayed notifications
    public static NotificationManager INSTANCE = new NotificationManager();
    private static int MAX_DISPLAYED = 5; // Maximum number of notifications to display at a time

    public static void addNotification(Notification notification) {
        if (ModuleManager.get(NotificationModule.class).isActive()) {
            notificationQueue.add(notification);
            updateNotifications();
        }
    }

    private static void updateNotifications() {
        // Remove expired notifications
        displayedNotifications.removeIf(Notification::isExpired);

        updatePositions();

        // Add notifications from the queue until the maximum number is reached
        while (displayedNotifications.size() < MAX_DISPLAYED && !notificationQueue.isEmpty()) {
            Notification notification = notificationQueue.poll();
            if (notification == null) return;

            notification.creationTime = System.currentTimeMillis();
            displayedNotifications.addFirst(notification);
            notification.playSound(notification.soundEvent, notification.volume, notification.pitch);
        }
    }

    private static void updatePositions() {
        int screenHeight = HeliosClient.MC.getWindow().getScaledHeight();
        int y = screenHeight - HEIGHT - 5;

        for (Notification notification : displayedNotifications) {
            notification.moveY(y - notification.targetY);
            y -= HEIGHT + 5;
        }
    }

    private static @NotNull MatrixStack scaleAndTranslateMatrix(RenderEvent event, Notification notification, float scale) {
        MatrixStack matrix = event.getDrawContext().getMatrices();

        matrix.push();

        // Translate to the center of the notification
        matrix.translate(notification.x + (float) notification.WIDTH / 2, notification.y + (float) notification.HEIGHT / 2, 0);

        // Scale the notification
        matrix.scale(scale, scale, 1.0f);

        // Translate back to the original position
        matrix.translate(-notification.x - (float) notification.WIDTH / 2, -notification.y - (float) notification.HEIGHT / 2, 0);
        return matrix;
    }

    public static void setMaxDisplayed(int maxDisplayed) {
        MAX_DISPLAYED = maxDisplayed;
    }

    @SubscribeEvent
    public void onTick(TickEvent.CLIENT tickEvent) {
        if (ModuleManager.get(NotificationModule.class).isActive()) {
            for (Notification notification : displayedNotifications) {
                notification.update();
            }
            updateNotifications();
        }
    }

    @SubscribeEvent
    public void render(RenderEvent event) {
        if (ModuleManager.get(NotificationModule.class).isActive()) {
            Iterator<Notification> iterator = displayedNotifications.descendingIterator();
            while (iterator.hasNext()) {
                Notification notification = iterator.next();
                float scale = Notification.ANIMATE == Notification.AnimationStyle.POP ? notification.scale : 1.0f;
                MatrixStack matrix = scaleAndTranslateMatrix(event, notification, scale);

                notification.render(matrix, notification.y, FontRenderers.Small_fxfontRenderer);
                matrix.pop();
            }
        }
    }
}
