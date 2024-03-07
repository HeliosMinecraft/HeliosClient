package dev.heliosclient.scripting;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class provides a Lua-friendly wrapper for Java objects.
 * It allows Lua scripts to interact with Java objects by exposing their public fields and methods.
 *
 * To be used in times when certain objects cant be {@link org.luaj.vm2.lib.jse.CoerceJavaToLua}
 */
public class LuaFriendlyWrapper {
    private final Object javaObject;

    /**
     * Constructs a new LuaFriendlyWrapper for the given Java object.
     *
     * @param javaObject the Java object to wrap
     */
    public LuaFriendlyWrapper(Object javaObject) {
        this.javaObject = javaObject;
    }

    /**
     * Gets the value of a public field of the wrapped Java object.
     *
     * @param fieldName the name of the field
     * @return the value of the field
     * @throws RuntimeException if the field does not exist or cannot be accessed
     */
    public Object get(String fieldName) {
        try {
            Field field = javaObject.getClass().getField(fieldName);
            return field.get(javaObject);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Calls a method of the wrapped Java object.
     *
     * @param methodName the name of the method
     * @param args the arguments to pass to the method
     * @return the result of the method call
     * @throws RuntimeException if the method does not exist, cannot be accessed, or throws an exception
     */
    public Object call(String methodName, Object... args) {
        try {
            Method method;
            if (args.length > 0) {
                method = javaObject.getClass().getMethod(methodName, args.getClass());
            } else {
                method = javaObject.getClass().getMethod(methodName);
            }
            return method.invoke(javaObject, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}