package com.seblit.rested.client.media;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used by {@link com.seblit.rested.client.ResourceFactory ResourceFactory} to parse the body object into the data that will be transferred
 * */
public interface RequestBodyParser {

    /**
     * Parses the body object into binary, formatted as the requested media type and encoded with the requested charset
     * @param body The {@link Object} that should be parsed
     * @param mediaType The media type which the result must be formatted as. This can only be a media type that this parser is registered for
     * @param charset The charset for the byte encoding
     * @throws Exception Any Exceptions thrown by this method will be thrown by the resource method that initiated the request
     * @return the binary body data
     * */
    byte @Nullable [] parse(@NotNull Object body, @NotNull String mediaType, @NotNull String charset) throws Exception;

}
