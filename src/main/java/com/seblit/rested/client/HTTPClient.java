package com.seblit.rested.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Client used by {@link ResourceFactory} to perform HTTP requests
 * */
public interface HTTPClient {

    /**
     * Performs the provided {@link Request} and returns a {@link Response} representing the servers response
     * @param request The {@link Request} that should be executed
     * @param method The resource method that initiated the request. May be used for inspection
     * @param params The parameters the resource method was called with. May be null if the method has no parameters
     * @throws Exception Any exceptions thrown by this method will be thrown by the resource method
     * @return a {@link Response} representing the servers response
     * */
    @NotNull
    Response request(@NotNull Request request, @NotNull Method method, @Nullable Object[] params) throws Exception;

}
