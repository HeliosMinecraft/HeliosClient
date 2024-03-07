package dev.heliosclient.managers;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.scripting.LuaEventManager;
import dev.heliosclient.system.HeliosExecutor;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class EventManager {
    private static final Map<Listener, Map<Class<?>, List<MethodHandle>>> INSTANCE = new ConcurrentHashMap<>();
    private static final Comparator<MethodHandle> METHOD_COMPARATOR = Comparator.comparingInt(mh -> {
        SubscribeEvent annotation = mh.type().parameterType(0).getAnnotation(SubscribeEvent.class);
        return (annotation != null) ? annotation.priority().ordinal() : Integer.MAX_VALUE;
    });
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Map<Class<?>, Long> LAST_POSTED = new ConcurrentHashMap<>();
    private static final long TIME_FRAME = TimeUnit.MINUTES.toMillis(1); // 1 minute timeframe

    public static void register(Listener listener) {
        Map<Class<?>, List<MethodHandle>> listenerMethods = new HashMap<>();

        for (Method method : listener.getClass().getMethods()) {
            if (method.isAnnotationPresent(SubscribeEvent.class) && method.getParameterCount() == 1) {
                Class<?> eventType = method.getParameterTypes()[0];
                MethodHandle methodHandle = getMethodHandle(method);
                List<MethodHandle> methodHandles = listenerMethods.computeIfAbsent(eventType, k -> new ArrayList<>());
                methodHandles.add(methodHandle);
                if (methodHandles.size() > 1) {
                    methodHandles.sort(METHOD_COMPARATOR);
                }
            }
        }
        INSTANCE.put(listener, listenerMethods);
    }

    private static MethodHandle getMethodHandle(Method method) {
        try {
            return LOOKUP.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void unregister(Listener listener) {
        INSTANCE.remove(listener);
    }

    public static Event postEvent(Event event) {
        Class<?> eventType = event.getClass();
        for (Map.Entry<Listener, Map<Class<?>, List<MethodHandle>>> entry : INSTANCE.entrySet()) {
            List<MethodHandle> methodHandles = entry.getValue().get(eventType);
            if (methodHandles != null) {
                for (MethodHandle methodHandle : methodHandles) {
                    try {
                        methodHandle.invoke(entry.getKey(), event);
                    } catch (Throwable e) {
                        handleException(e, entry.getKey(), event);
                    }
                }
            }
        }
        if (event.getClass().isAnnotationPresent(LuaEvent.class) && LuaEventManager.INSTANCE.hasListeners()) {
            HeliosExecutor.execute(() -> LuaEventManager.INSTANCE.post(event.getClass().getAnnotation(LuaEvent.class).value(), CoerceJavaToLua.coerce(event)));
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

        for (Map.Entry<Listener, Map<Class<?>, List<MethodHandle>>> entry : INSTANCE.entrySet()) {
            List<MethodHandle> methodHandles = entry.getValue().get(eventType);
            if (methodHandles != null) {
                for (MethodHandle methodHandle : methodHandles) {
                    try {
                        methodHandle.invoke(entry.getKey(), event);
                    } catch (Throwable e) {
                        handleException(e, entry.getKey(), event);
                    }
                }
            }
        }
    }


    private static void handleException(Throwable e, Listener listener, Event event) {
        HeliosClient.LOGGER.info("Exception occurred while processing event: " + event.getClass().getName() + " \n Following was the listener: " + listener, e);
        HeliosClient.LOGGER.warn("An error occurred while processing an event. Please check the log file for details.");
    }

}

