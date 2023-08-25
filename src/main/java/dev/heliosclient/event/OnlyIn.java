package dev.heliosclient.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// This annotation indicates that a method should only run on a specific side
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnlyIn {
    // This attribute specifies the side (client or server)
    Dist value();
}
