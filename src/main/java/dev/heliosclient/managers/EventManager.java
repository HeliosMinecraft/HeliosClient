package dev.heliosclient.managers;

import dev.heliosclient.event.Event;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.KeyPressedEvent;
import dev.heliosclient.event.listener.Listener;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class EventManager {
    private static final Set<Listener> INSTANCE = new CopyOnWriteArraySet<>();

    public static void register(Listener listener) {
        INSTANCE.add(listener);
    }

    public static void unregister(Listener listener) {
        INSTANCE.remove(listener);
    }

    public static void postEvent(Event event) {
        if (!INSTANCE.isEmpty()) {
            for (Listener listener : INSTANCE) {
                for (Method method : listener.getClass().getDeclaredMethods()) {
                    if (method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom(event.getClass()) && method.isAnnotationPresent(SubscribeEvent.class)) {
                        try {
                            method.invoke(listener, event);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}

