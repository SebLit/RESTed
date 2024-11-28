package com.seblit.rested.client.media;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown by {@link com.seblit.rested.client.ResourceFactory ResourceFactory} when a request with body was made but no
 * {@link RequestBodyParser} was registered for the required media type
 * */
public class MissingRequestParserException extends MissingParserException {

    /**
     * Creates a new instance
     * @param mediaType The media type that had no parser registered
     * */
    public MissingRequestParserException(@NotNull String mediaType) {
        super(mediaType);
    }

}
