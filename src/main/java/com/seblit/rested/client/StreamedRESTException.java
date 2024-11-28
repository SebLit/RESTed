package com.seblit.rested.client;

import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

/**
 * Declare this or a subtype of this Exception using {@link Error} to cause a {@link RESTException} with an {@link InputStream}
 * to access the response body instead of having it parsed by {@link ResourceFactory}
 * */
public class StreamedRESTException extends RESTException implements AutoCloseable {

    private InputStream bodyStream;

    public StreamedRESTException() {
        super();
    }

    public StreamedRESTException(@Nullable String message) {
        super(message);
    }

    public StreamedRESTException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public StreamedRESTException(@Nullable Throwable cause) {
        super(cause);
    }

    protected StreamedRESTException(@Nullable String message, @Nullable Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Note that this Stream may already have been used and invalidated by others
     * @return an {@link InputStream} to read the response body from. May be null if no body was received
     * */
    @Nullable
    public InputStream getBodyStream() {
        return bodyStream;
    }

    /**
     * Closes this responses body {@link InputStream} if available
     */
    @Override
    public void close() throws Exception {
        if (bodyStream != null) {
            bodyStream.close();
        }
    }

    final void init(@Nullable InputStream bodyStream) {
        this.bodyStream = bodyStream;
    }
}
