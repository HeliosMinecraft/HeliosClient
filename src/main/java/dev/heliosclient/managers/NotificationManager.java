package dev.heliosclient.managers;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.render.RenderEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.ui.notification.Notification;
import dev.heliosclient.util.fontutils.FontRenderers;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class NotificationManager implements Listener {
    public static NotificationManager INSTANCE = new NotificationManager();
    private static final int HEIGHT = 25;
    private static final Deque<Notification> notifications = new ArrayDeque<>();

    public static void addNotification(Notification notification) {
        if (ModuleManager.notificationModule.isActive()) {
            notifications.addFirst(notification);
            updatePositions();
        }
    }

    private static void updatePositions() {
        int screenHeight = HeliosClient.MC.getWindow().getScaledHeight();
        int y = screenHeight - HEIGHT - 5;

        for (Notification notification : notifications) {
            notification.moveY(y - notification.targetY);
            y -= HEIGHT + 5;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.CLIENT tickEvent) {
        if (ModuleManager.notificationModule.isActive()) {
            notifications.removeIf(Notification::isExpired);
            for (Notification notification : notifications) {
                notification.update();
            }
            updatePositions();
        }
    }

    @SubscribeEvent
    public void render(RenderEvent event) {
        if (ModuleManager.notificationModule.isActive()) {
            Iterator<Notification> iterator = notifications.descendingIterator();
            while (iterator.hasNext()) {
                Notification notification = iterator.next();
                notification.render(event.getDrawContext().getMatrices(), notification.y, FontRenderers.Small_fxfontRenderer);
            }
        }
    }
}
