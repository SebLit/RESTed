package com.seblit.rested.client.annotation;

import java.lang.annotation.*;

/**
 * Declares specific {@link Throwable}s for certain http statuscode error ranges (All excluding 200-299).<br>
 * Undeclared ranges default to {@link com.seblit.rested.client.RESTException RESTException}
 * */
@Repeatable(Errors.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Error {

    /**
     * The {@link Throwable} type that should be thrown
     * */
    Class<? extends Throwable> value();
    /**
     * The start of the code range. 200-299 will be ignored.<br>
     * Default: 1
     * */
    int startCode() default 1;
    /**
     * The end of the code range. 200-299 will be ignored.<br>
     * Default: 599
     * */
    int endCode() default 599;

}
