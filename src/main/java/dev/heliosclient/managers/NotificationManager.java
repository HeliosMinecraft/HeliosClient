package dev.heliosclient.managers;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.FontChangeEvent;
import dev.heliosclient.event.events.RenderEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.util.fontutils.fxFontRenderer;
import dev.heliosclient.util.notification.Notification;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class NotificationManager implements Listener {
    private static final int HEIGHT = 25;
    private final Deque<Notification> notifications = new ArrayDeque<>();
    private fxFontRenderer fontRenderer;

    public NotificationManager() {
        if (HeliosClient.MC.getWindow() != null) {
            fontRenderer = new fxFontRenderer(FontManager.fonts, 6);
        }
    }

    public void addNotification(Notification notification) {
        notifications.addFirst(notification);
        updatePositions();
    }

    private void updatePositions() {
        int screenHeight = HeliosClient.MC.getWindow().getScaledHeight();
        int y = screenHeight - HEIGHT - 5;

        for (Notification notification : notifications) {
            notification.moveY(y - notification.targetY);
            y -= HEIGHT + 5;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.CLIENT tickEvent) {
        notifications.removeIf(Notification::isExpired);
        for (Notification notification : notifications) {
            notification.update();
        }
        updatePositions();
    }

    @SubscribeEvent
    public void render(RenderEvent event) {

        Iterator<Notification> iterator = notifications.descendingIterator();
        while (iterator.hasNext()) {
            Notification notification = iterator.next();
            notification.render(event.getDrawContext().getMatrices(), notification.currentY, fontRenderer);
        }
    }

    @SubscribeEvent
    public void onFontChange(FontChangeEvent event) {
        fontRenderer = new fxFontRenderer(event.getFonts(), 6);
    }
}
