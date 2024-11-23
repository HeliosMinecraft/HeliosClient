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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NotificationManager implements Listener {
    private static final Queue<Notification> notificationQueue = new ConcurrentLinkedQueue<>(); // Queue to hold notifications
    private static final Deque<Notification> displayedNotifications = new ArrayDeque<>(); // Deque to hold currently displayed notifications
    public static NotificationManager INSTANCE = new NotificationManager();
    private static int MAX_DISPLAYED = 5; // Maximum number of notifications to display at a time
    private static Notification.PositionMode POSITION_MODE = Notification.PositionMode.BOTTOM_RIGHT;
    private static int VERTICAL_SPACING = 5, HORIZONTAL_SPACING = 5;

    private NotificationManager() {}

    /**
     * Add a notification to the queue
     * @param notification Notification to add
     */
    public static void addNotification(Notification notification) {
        if (ModuleManager.get(NotificationModule.class).isActive()) {
            notification.setPositionMode(POSITION_MODE);

            notificationQueue.add(notification);
            updateNotifications();
        }
    }
    /**
     * Clear all notifications
     */
    public void clear(){
        notificationQueue.clear();
        displayedNotifications.clear();
    }

    /**
     * Update notifications, managing queue and displayed notifications
     */
    private static void updateNotifications() {
        // Remove expired notifications
        displayedNotifications.removeIf(Notification::isExpired);

        // Add notifications from the queue until the maximum number is reached
        while (displayedNotifications.size() < MAX_DISPLAYED && !notificationQueue.isEmpty()) {
            Notification notification = notificationQueue.poll();
            if (notification == null) return;

            notification.setCreationTime(System.currentTimeMillis());
            notification.playSound(notification.soundEvent, notification.volume, notification.pitch);
            displayedNotifications.addFirst(notification);
        }

        updatePositions();
    }

    private static void updatePositions() {
        if(HeliosClient.MC.getWindow() == null) return;

        int screenWidth = HeliosClient.MC.getWindow().getScaledWidth();
        int screenHeight = HeliosClient.MC.getWindow().getScaledHeight();
        int targetY = (POSITION_MODE == Notification.PositionMode.BOTTOM_RIGHT ||
                POSITION_MODE == Notification.PositionMode.BOTTOM_LEFT)
                ? screenHeight - VERTICAL_SPACING
                : VERTICAL_SPACING;
        int targetX = calculateInitialTargetX(screenWidth);

        for (Notification notification : displayedNotifications) {
            // Special handling for CENTER position
            if (POSITION_MODE == Notification.PositionMode.CENTER) {
                // Keep the notification at the center without vertical stacking
                int centerX = (screenWidth - notification.getWidth()) / 2 + HORIZONTAL_SPACING;
                int centerY = (screenHeight - notification.getHeight()) / 2 + VERTICAL_SPACING;
                notification.smoothMoveY(centerY);
                notification.setTargetX(centerX);
                continue;
            }

            // Calculate target vertical offset based on position mode
            int targetOffset = switch (POSITION_MODE) {
                case BOTTOM_RIGHT, BOTTOM_LEFT -> {
                    targetY -= (notification.getHeight() + VERTICAL_SPACING);
                    yield targetY;
                }
                case TOP_RIGHT, TOP_LEFT -> {
                    int offset = targetY;
                    targetY += notification.getHeight() + VERTICAL_SPACING;
                    yield offset;
                }
                default -> targetY;
            };

            // Update X position based on position mode and horizontal spacing
            int newX = calculateNotificationX(screenWidth, notification.getWidth(), targetX);

            notification.smoothMoveY(targetOffset);
            notification.setTargetX(newX);
        }
    }
    private static int calculateInitialTargetX(int screenWidth) {
        return switch (POSITION_MODE) {
            case BOTTOM_RIGHT, TOP_RIGHT -> screenWidth - HORIZONTAL_SPACING;
            case BOTTOM_LEFT, TOP_LEFT -> HORIZONTAL_SPACING;
            case CENTER -> (screenWidth - HORIZONTAL_SPACING) / 2;
        };
    }

    private static int calculateNotificationX(int screenWidth, int notificationWidth, int targetX) {
        return switch (POSITION_MODE) {
            case BOTTOM_RIGHT, TOP_RIGHT -> targetX - notificationWidth;
            case BOTTOM_LEFT, TOP_LEFT -> targetX;
            case CENTER -> (screenWidth - notificationWidth) / 2;
        };
    }

    // Add method to set horizontal spacing
    public static void setHorizontalSpacing(int spacing) {
        HORIZONTAL_SPACING = Math.max(0, spacing);
    }

    private static @NotNull MatrixStack scaleAndTranslateMatrix(RenderEvent event, Notification notification, float scale) {
        MatrixStack matrix = event.getDrawContext().getMatrices();

        matrix.push();

        // Translate to the center of the notification
        matrix.translate(
                notification.getX() + (float) notification.getWidth() / 2,
                notification.getY() + (float) notification.getHeight() / 2,
                0
        );

        // Scale the notification
        matrix.scale(scale, scale, 1.0f);

        // Translate back to the original position
        matrix.translate(
                -notification.getX() - (float) notification.getWidth() / 2,
                -notification.getY() - (float) notification.getHeight() / 2,
                0
        );
        return matrix;
    }

    public static void setMaxDisplayed(int maxDisplayed) {
        MAX_DISPLAYED = maxDisplayed;
    }

    public static void setPositionMode(Notification.PositionMode positionMode) {
        POSITION_MODE = positionMode;
        updatePositions();
    }

    public static void setVerticalSpacing(int spacing) {
        VERTICAL_SPACING = Math.max(0, spacing);
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
                float scale = Notification.ANIMATE == Notification.AnimationStyle.POP ? notification.getScale() : 1.0f;
                MatrixStack matrix = scaleAndTranslateMatrix(event, notification, scale);

                notification.render(matrix, notification.getY(), FontRenderers.Small_fxfontRenderer);
                matrix.pop();
            }
        }
    }
}
