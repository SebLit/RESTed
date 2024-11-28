package com.seblit.rested.client.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a parameter as a query parameter.<br>
 * Parameter may be an array or an {@link Iterable}, in which case each contained value is added separately for the same query parameter.
 * Otherwise, the parameter will be converted to a String using {@link String#valueOf(Object)}. Null values are skipped
 * */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryParam {

    /**
     * The name of the query parameter
     * */
    String value();

}
