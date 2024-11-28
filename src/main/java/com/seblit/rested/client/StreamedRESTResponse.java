package com.seblit.rested.client;

import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

/**
 * Use this as return type of a resource method to get a {@link RESTResponse} that accesses the response body through an {@link InputStream} instead of
 * having it parsed by {@link ResourceFactory}
 * */
public final class StreamedRESTResponse extends RESTResponse implements AutoCloseable{

    private final InputStream bodyStream;

    /**
     * Creates a new instance
     * @param bodyStream The {@link InputStream} for the response body. May be null if no body was received
     * */
    public StreamedRESTResponse(@Nullable InputStream bodyStream){
        this.bodyStream = bodyStream;
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
        if(bodyStream != null) {
            bodyStream.close();
        }
    }
}
