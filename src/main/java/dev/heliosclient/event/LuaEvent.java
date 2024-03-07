package dev.heliosclient.event;

import org.luaj.vm2.LuaValue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates that the associated event can be utilized within Lua scripts.
 * The {@link #value()} method returns the name of the event as it should be referenced within Lua scripts.
 *
 * <p>Here's an example of how to use this in a Lua script:</p>
 * <pre><code>
 * -- Define a function to handle the block break event
 * local function onBlockBreak(event)
 *     -- Get the position of the block break event
 *     local getPos = event:getPos()
 *     -- Print the position
 *     print(getPos)
 *     -- Print the x-coordinates from the BlockPos object
 *     print(pos:getX())
 *
 *     return true, "Success"
 * end
 *
 * -- Register the function as a handler for the block break event
 * eventManager:register("BlockBreakEvent", onBlockBreak)
 * </code></pre>
 *
 * <p>In the above Lua script, {@code 'eventManager'} is a globally registered instance of {@link dev.heliosclient.scripting.LuaEventManager}
 * which is defined in {@link dev.heliosclient.scripting.LuaExecutor#globals}. It is used to register Lua functions as event handlers.</p>
 * <p>
 * At the end of the function we return true and the string success.
 * This is to point to the event manager that the event has been posted and computed successfully. {@link dev.heliosclient.scripting.LuaEventManager#post(String, LuaValue)}
 * No return statement will return nil.
 * </p>
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LuaEvent {
    String value();
}