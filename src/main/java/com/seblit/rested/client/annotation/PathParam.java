package com.seblit.rested.client.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a parameter for insertion into the {@link Endpoint} path. Null values will not be inserted.<br>
 * Placeholders in the {@link Endpoint} path must be surrounded by {parentheses}
 * */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PathParam {

    /**
     * Name of the placeholder in the {@link Endpoint} path without parentheses
     * */
    String value();

}
