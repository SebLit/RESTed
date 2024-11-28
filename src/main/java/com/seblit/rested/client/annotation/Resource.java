package com.seblit.rested.client.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a base path of a resource interface that is added as a prefix to all {@link Endpoint} paths in the interface
 * */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Resource {

    /**
     * The base path. Should start with slash and end without.<br>
     * May contain placeholders for {@link PathParam} which must be declared by all {@link Endpoint} methods in the resource interface.<br>
     * */
    String value();

}
