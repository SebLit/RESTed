package com.seblit.rested.client;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Use or extend this class to access response http status code, message and headers
 * */
public class RESTResponse extends HeaderHolder {

    private int statusCode;
    private String responseMessage;

    /**
     * Creates a new instance
     * */
    public RESTResponse() {
        super(null);
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
    public String getResponseMessage() {
        return responseMessage;
    }

    final void init(int statusCode, @Nullable String message, @Nullable Map<String, List<String>> headers) {
        this.statusCode = statusCode;
        this.responseMessage = message;
        setHeaders(headers);
    }

}
