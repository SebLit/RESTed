package com.seblit.rested.client.media;

import com.seblit.rested.client.Request;
import com.seblit.rested.client.Response;
import org.jetbrains.annotations.NotNull;

/**
 * Used by {@link com.seblit.rested.client.ResourceFactory ResourceFactory} to parse response body binary data into the required object type
 * */
public interface ResponseBodyParser {

    /**
     * Parses the binary data into the requested object type
     * @param type The object type to create. Note: May also be a {@link Throwable} subtype for error responses
     * @param request The original {@link Request}. May be used for further inspection
     * @param response The received {@link Response}. Access {@link Response#getBodyStream()} to read the response binary body data
     * @param mediaType The media type with which the binary data is formatted
     * @param charset The charset with which the binary data is encoded
     * @throws Exception Any Exceptions thrown by this method will be thrown by the resource method that initiated the request
     * @return the object that was parsed from the binary data
     * */
    @NotNull
    <T> T parse(@NotNull Class<T> type, @NotNull Request request, @NotNull Response response, @NotNull String mediaType, @NotNull String charset) throws Exception;

}
