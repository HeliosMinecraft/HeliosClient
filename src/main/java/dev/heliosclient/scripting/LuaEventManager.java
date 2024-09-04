package dev.heliosclient.scripting;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.ColorUtils;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class manages Lua event listeners.
 */
public class LuaEventManager {
    public static LuaEventManager INSTANCE = new LuaEventManager();
    private final Map<String, List<LuaFunction>> listeners = new HashMap<>();

    /**
     * Registers a Lua event listener.
     *
     * @param eventType The type of the event.
     * @param listener  The Lua function to call when the event is fired.
     */
    public void register(String eventType, LuaValue listener) {
        LuaFunction luaFunction = listener.checkfunction();
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(luaFunction);
    }

    /**
     * Unregisters a Lua event listener.
     *
     * @param eventType The type of the event.
     * @param listener  The Lua function to remove.
     */
    public void unregister(String eventType, LuaValue listener) {
        LuaFunction luaFunction = listener.checkfunction();
        List<LuaFunction> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
           eventListeners.remove(luaFunction);
        }
    }

    /**
     * Clears all event listeners for a specific Lua file.
     *
     * @param luaFile The Lua file whose listeners should be removed.
     */
    public void clearListeners(LuaFile luaFile) {
        List<String> unregisteredEvents = new ArrayList<>();
        for (Map.Entry<String, List<LuaFunction>> entry : listeners.entrySet()) {
            List<LuaFunction> eventListeners = entry.getValue();
            boolean removed = eventListeners.removeIf(listener -> listener.tojstring().contains(luaFile.getScriptName()));
            if (removed) {
                unregisteredEvents.add(entry.getKey());
            }
        }
        if (!unregisteredEvents.isEmpty()) {
            HeliosClient.LOGGER.warn("Warning: Unregistered events for Lua file {}: {}", luaFile.getName(), unregisteredEvents);
            ChatUtils.sendHeliosMsg(ColorUtils.yellow + "Warning: Unregistered events for Lua file " + ColorUtils.aqua + luaFile.getName() + ColorUtils.darkMagenta + ": " + unregisteredEvents);
            ChatUtils.sendHeliosMsg(ColorUtils.yellow + "The above event listeners were removed automatically. Please remove them manually in the code during `onStop()` call for the better!");
        }
    }

    /**
     * Checks if there are any Lua event listeners.
     *
     * @return True if there are any Lua event listeners, false otherwise.
     */
    public boolean hasListeners() {
        return !listeners.isEmpty();
    }

    /**
     * Posts an event to all registered Lua event listeners of a specific type.
     * <p>
     * This method retrieves all Lua event listeners registered for the given event type,
     * and then invokes each listener with the provided event data. The invocation is done
     * in protected mode to prevent Lua errors from crashing the Java application.
     * <p>
     * If an error occurs while invoking a Lua function, it logs the error and continues
     * with the next listener. After each invocation, it checks the status returned by the
     * Lua function. If the status is false or nil, it logs an error message with the result
     * returned by the Lua function.
     * <p>
     * Check {@link dev.heliosclient.event.LuaEvent} for code example.
     * </p>
     *
     * @param eventType The type of the event.
     * @param event     The event data.
     */
    public void post(String eventType, LuaValue event) {
        List<LuaFunction> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            for (LuaFunction listener : eventListeners) {
                LuaValue status = LuaValue.FALSE;
                LuaValue result = LuaValue.NIL;
                try {
                    // Call the listener in protected mode
                    Varargs varargs = listener.invoke(event);
                    status = varargs.arg1();
                    result = varargs.arg(2);
                } catch (Throwable e) {
                    HeliosClient.LOGGER.error("Error while invoking Lua function", e);
                    ChatUtils.sendHeliosMsg("Error while invoking LuaFunction: " + e.getMessage());
                }
                // Check if an error occurred
                if (!status.toboolean() && !status.isnil()) {
                    HeliosClient.LOGGER.error("Error in Lua script: {}", result.tojstring());
                    ChatUtils.sendHeliosMsg("Error in Lua script: " + result.tojstring());
                }
            }
        }
    }

}