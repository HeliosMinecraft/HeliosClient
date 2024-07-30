package dev.heliosclient.managers;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.input.KeyPressedEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.scripting.LuaEventManager;
import dev.heliosclient.util.TimerUtils;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

public class EventManager {
    private static final Map<Class<?>, List<EventListener>> listeners = new ConcurrentHashMap<>();

    private static final Comparator<EventListener> METHOD_COMPARATOR = Comparator.comparingInt(el -> {
        SubscribeEvent annotation = el.method.getAnnotation(SubscribeEvent.class);
        return (annotation != null) ? annotation.priority().ordinal() : Integer.MIN_VALUE;
    });

    private static final Map<Class<?>, Long> LAST_POSTED = new ConcurrentHashMap<>();

    private static final long TIME_FRAME = TimeUnit.MINUTES.toMillis(1); // 1 minute timeframe

    private static final Executor executor = Executors.newFixedThreadPool(10);

    public static void register(Listener listener) {
        for (Method method : listener.getClass().getMethods()) {
            if (method.isAnnotationPresent(SubscribeEvent.class) && method.getParameterCount() == 1) {
                Class<?> eventType = method.getParameterTypes()[0];
                EventListener eventListener = new EventListener(listener, method);

                List<EventListener> eventListeners = getListeners(eventType);

                // Check for duplicates before adding
                boolean alreadyRegistered = eventListeners.stream()
                        .anyMatch(el -> el.listener == listener);

                if (!alreadyRegistered) {
                    eventListeners.add(eventListener);
                    if (eventListeners.size() > 1) {
                        eventListeners.sort(METHOD_COMPARATOR);
                    }
                }
            }
        }
    }

    private static List<EventListener> getListeners(Class<?> eventClass) {
        return listeners.computeIfAbsent(eventClass, key -> new CopyOnWriteArrayList<>());
    }


    public static void unregister(Listener listener) {
        for (List<EventListener> eventListeners : listeners.values()) {
            eventListeners.removeIf(el -> el.listener == listener || el.listener == null);

            if(eventListeners.isEmpty()){
                listeners.values().remove(eventListeners);
            }
        }
    }

    /**
     * @param event Event to post
     * @return Same event returned.
     */
    public static Event postEvent(Event event) {
            List<EventListener> eventListeners = listeners.get(event.getClass());

            if (eventListeners == null || eventListeners.isEmpty()) return event;

            for (EventListener listener : eventListeners) {
                if(listener == null){
                    eventListeners.remove(listener);
                    continue;
                }
                listener.accept(event);
            }

            if (event.getClass().isAnnotationPresent(LuaEvent.class) && LuaEventManager.INSTANCE.hasListeners()) {
                executor.execute(() -> LuaEventManager.INSTANCE.post(event.getClass().getAnnotation(LuaEvent.class).value(), CoerceJavaToLua.coerce(event)));
            }

        return event;
    }

    /**
     * Posts an event but make sure that the event hasn't been posted before within the given timeframe.
     * If it has been posted within the said timeframe before, it won't post another one.
     *
     * @param event      Event to be posted
     * @param TIME_FRAME Time frame to check
     */
    @Deprecated
    public static void postEvent(Event event, long TIME_FRAME) {
        Class<?> eventType = event.getClass();
        Long lastPosted = LAST_POSTED.get(eventType);
        long currentTime = System.currentTimeMillis();

        if (lastPosted != null && (currentTime - lastPosted) < TIME_FRAME) {
            return; // Skip posting this event as it was already posted within the timeframe
        }

        LAST_POSTED.put(eventType, currentTime); // Update the last posted time

        postEvent(event);
    }

    private static void handleException(Throwable e, Listener listener, Event event) {
        HeliosClient.LOGGER.error("Exception occurred while processing event: {} \n Following was the listener: {}", event.getClass().getName(), listener, e);

    }

    private record EventListener(Listener listener, Method method) {
        public void accept(Event event) {
            try {
                method.invoke(listener, event);
            } catch (Exception e) {
                handleException(e, listener, event);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof EventListener el){
                return el.listener == this.listener && el.method == this.method;
            }
            return false;
        }
    }

}

