package dev.heliosclient.util.misc;

import java.lang.reflect.Method;

/**
 * uhh why do we need this again?
 */
public class ReflectionUtils {
    public static Method tryGetMethod(String methodName, Class<?> class1) {
        // safety be damned this is my own code I get to control when it crashes
        try {
            return class1.getDeclaredMethod(methodName, new Class[1]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void tryCallMethod(Method method, Object... parameters) {
        // hope that shits static
        try {
            method.invoke(null, parameters);
        } catch (Exception e) {
            // go fuck yourself
            e.printStackTrace();
        }

    }
}
