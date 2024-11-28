package com.seblit.rested.client.middleware;

import com.seblit.rested.client.Request;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Thrown by a {@link RequestInterceptor} to abort execution of a {@link Request Request}. May be extended to add additional details
 * */
public class RequestInterceptedException extends InterceptedException {

    private final Request request;

    public RequestInterceptedException(@NotNull Request request) {
        super();
        this.request = request;
    }

    public RequestInterceptedException(@NotNull Request request, @Nullable String message) {
        super(message);this.request = request;
    }

    public RequestInterceptedException(@NotNull Request request, @Nullable String message, @Nullable Throwable cause) {
        super(message, cause);this.request = request;
    }

    public RequestInterceptedException(@NotNull Request request, @Nullable Throwable cause) {
        super(cause);this.request = request;
    }

    protected RequestInterceptedException(@NotNull Request request, @Nullable String message, @Nullable Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);this.request = request;
    }

    /**
     * @return the intercepted {@link Request}
     * */
    @NotNull
    public Request getRequest() {
        return request;
    }
}