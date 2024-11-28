package com.seblit.rested.client.media;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown by {@link com.seblit.rested.client.ResourceFactory ResourceFactory} when a request or response needed parsing,
 * but no {@link RequestBodyParser} or {@link ResponseBodyParser} was registered for the required media type
 * */
public class MissingParserException extends RuntimeException {

    private static final String FORMAT_MESSAGE = "No parser registered for media type %s";
    private final String mediaType;

    /**
     * Creates a new instance
     * @param mediaType The media type that had no parser registered
     * */
    public MissingParserException(@NotNull String mediaType) {
        super(String.format(FORMAT_MESSAGE, mediaType));
        this.mediaType = mediaType;
    }

    /**
     * @return the media type
     * */
    @NotNull
    public String getMediaType() {
        return mediaType;
    }
}
