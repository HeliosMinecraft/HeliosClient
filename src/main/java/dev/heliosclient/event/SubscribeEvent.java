package dev.heliosclient.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubscribeEvent {
    enum Priority {
        LOWEST,
        LOW,
        NORMAL,
        HIGH,
        HIGHEST
    }

    Priority priority() default Priority.NORMAL;
}



