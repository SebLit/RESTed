package com.seblit.rested.client.middleware;

import org.jetbrains.annotations.Nullable;

/**
 * Superclass for {@link RequestInterceptedException} and {@link ResponseInterceptedException} which may be used in catchphrases to handle both types in the same block
 * */
public class InterceptedException extends Exception {

     InterceptedException() {
        super();
    }

     InterceptedException(@Nullable String message) {
        super(message);
    }

     InterceptedException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

     InterceptedException(@Nullable Throwable cause) {
        super(cause);
    }

     InterceptedException(@Nullable String message, @Nullable Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
