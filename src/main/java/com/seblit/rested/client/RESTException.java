package com.seblit.rested.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thrown by resource methods when an error response is received and no other Exception type was declared through {@link Error}.<br>
 * Provides access to response status code, message, headers and the original request
 * */
public class RESTException extends RuntimeException {

    private final Map<String, List<String>> headers = new HashMap<>();
    private int statusCode;
    private String responseMessage;
    private Request request;

    public RESTException() {
        super();
    }

    public RESTException(@Nullable String message) {
        super(message);
    }

    public RESTException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public RESTException(@Nullable Throwable cause) {
        super(cause);
    }

    protected RESTException(@Nullable String message, @Nullable Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    final void init(int statusCode, @Nullable String responseMessage, @Nullable Map<String, List<String>> headers, @NotNull Request request) {
        this.statusCode = statusCode;
        this.responseMessage = responseMessage;
        this.request = request;
        if (headers != null) {
            headers.forEach((name, values) -> this.headers.put(name, new ArrayList<>(values)));
        }
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
    public String getResponseMessage() {
        return responseMessage;
    }

    /**
     * @return the {@link Request} that lead to this response
     */
    @NotNull
    public Request getRequest() {
        return request;
    }

    /**
     * @return an array containing all headers of this response
     */
    @NotNull
    public String[] getHeaders() {
        return headers.keySet().toArray(new String[0]);
    }

    /**
     * @return true if this response contains the requested header
     */
    public boolean hasHeader(@Nullable String header) {
        return headers.containsKey(header);
    }

    /**
     * @param header The desired header
     * @return an array containing all values that this object holds for the requested header. May be null if the header isn't present
     */
    @Nullable
    public String[] getHeaderValues(String header) {
        return hasHeader(header) ? headers.get(header).toArray(new String[0]) : null;
    }
}
