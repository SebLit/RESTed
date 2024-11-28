package com.seblit.rested.client.media;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown by {@link com.seblit.rested.client.ResourceFactory ResourceFactory} when a response with body was received but no
 * {@link RequestBodyParser} was registered for the required media type
 * */
public class MissingResponseParserException extends MissingParserException {

    /**
     * Creates a new instance
     * @param mediaType The media type that had no parser registered
     * */
    public MissingResponseParserException(@NotNull String mediaType) {
        super(mediaType);
    }

}
