package com.seblit.rested.client;

/**
 * Represents the http method of a {@link Request}
 * */
public enum RequestMethod {

    GET( true),
    POST( true),
    DELETE( true),
    HEAD( false),
    PUT(   true),
    PATCH(true);

    private final boolean isResponseBodySupported;

    RequestMethod(boolean isResponseBodySupported){
        this.isResponseBodySupported = isResponseBodySupported;
    }

    /**
     * May be used by {@link HTTPClient} to decide whether or not to expect a response body
     * @return whether or not the method may have a response body.
     * */
    public boolean isResponseBodySupported() {
        return isResponseBodySupported;
    }
}
