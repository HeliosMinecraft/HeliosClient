package dev.heliosclient.event;

import dev.heliosclient.event.listener.Listener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventManager {
    private static final List<Listener> listeners = new CopyOnWriteArrayList<>();

    public static void register(Listener listener) {
        listeners.add(listener);
    }

    public static void unregister(Listener listener) {
        listeners.remove(listener);
    }

    public static void postEvent(Event event) {
        if (!listeners.isEmpty()) {
            for (Listener listener : listeners) { // Create a new list for iteration
                for (Method method : listener.getClass().getMethods()) {
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

