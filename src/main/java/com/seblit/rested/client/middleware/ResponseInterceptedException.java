package com.seblit.rested.client.middleware;

import com.seblit.rested.client.Request;
import com.seblit.rested.client.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Thrown by a {@link ResponseInterceptor} to abort execution of a {@link Request Request} after the {@link Response} was received. May be extended to add additional details
 * */
public class ResponseInterceptedException extends InterceptedException {

    private final Request request;
    private final Response response;

    public ResponseInterceptedException(@NotNull Request request, @NotNull Response response) {
        super();
        this.request = request;
        this.response = response;
    }

    public ResponseInterceptedException(@NotNull Request request, @NotNull Response response, @Nullable String message) {
        super(message);
        this.request = request;
        this.response = response;
    }

    public ResponseInterceptedException(@NotNull Request request, @NotNull Response response, @Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
        this.request = request;
        this.response = response;
    }

    public ResponseInterceptedException(@NotNull Request request, @NotNull Response response, @Nullable Throwable cause) {
        super(cause);
        this.request = request;
        this.response = response;
    }

    protected ResponseInterceptedException(@NotNull Request request, @NotNull Response response, @Nullable String message, @Nullable Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.request = request;
        this.response = response;
    }

    /**
     * @return the intercepted {@link Request}
     * */
    @NotNull
    public Request getRequest() {
        return request;
    }

    /**
     * Note: At this point the {@link Response#getBodyStream()} may have been invalidated and can't be read again
     * @return the {@link Response} to the intercepted request.
     * */
    @NotNull
    public Response getResponse() {
        return response;
    }
}