package com.seblit.rested.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Represents a response that is received by a {@link Request} executed by a {@link HTTPClient}
 */
public class Response extends HeaderHolder implements AutoCloseable {

    private final int statusCode;
    private final String message;
    private final InputStream bodyStream;

    /**
     * Creates a new instance
     *
     * @param statusCode The http status code of the response
     * @param message    The http response message. May be null if none was received
     * @param bodyStream An {@link InputStream} to read the body of the response. May be null if no body is expected/desired (i.e. {@link RequestMethod}s that don't allow response bodies)
     * @param headers    The response headers. May be null if none were received
     */
    public Response(int statusCode, @Nullable String message, @Nullable InputStream bodyStream, @Nullable Map<String, List<String>> headers) {
        super(headers);
        this.statusCode = statusCode;
        this.message = message;
        this.bodyStream = bodyStream;
    }

    /**
     * @return the http status code of this response
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @return the http response message. May be null if none was received
     */
    @Nullable
    public String getMessage() {
        return message;
    }

    /**
     * Note that this Stream may already have been used and invalidated by others
     *
     * @return an {@link InputStream} to read the body of the response. May be null if there is no response body
     */
    @Nullable
    public InputStream getBodyStream() {
        return bodyStream;
    }

    /**
     * @return true for 2xx status codes, false otherwise
     */
    public boolean isSuccessResponse() {
        return statusCode / 100 == 2;
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
}
