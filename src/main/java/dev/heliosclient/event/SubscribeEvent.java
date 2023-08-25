package dev.heliosclient.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// This annotation indicates that a method is a listener for an event
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubscribeEvent {
    // This attribute specifies the priority of the listener
    EventPriority priority() default EventPriority.NORMAL;
    Dist side() default Dist.BOTH;
}


