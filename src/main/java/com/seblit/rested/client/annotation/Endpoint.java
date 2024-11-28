package com.seblit.rested.client.annotation;

import com.seblit.rested.client.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface method as a REST endpoint
 * */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Endpoint {

    /**
     * The {@link RequestMethod} of this endpoint
     * */
    RequestMethod value();
    /**
     * The path to this endpoint. Should start with slash and end without.<br>
     * May contain placeholders for {@link PathParam}.<br>
     * {@link Resource} may add a prefix to this path.<br>
     * Default: empty
     * */
    String path() default "";
    /**
     * The media types for the Accept header.<br>
     * Default: {@link Body#JSON JSON}
     * */
    String[] mediaTypes() default {Body.JSON};
    /**
     * The charsets for the Accept-Charset header.<br>
     * Default: UTF-8
     * */
    String[] charsets() default {"UTF-8"};
}
