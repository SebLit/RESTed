package com.seblit.rested.client.middleware;

import com.seblit.rested.client.Request;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Notified by a {@link com.seblit.rested.client.ResourceFactory ResourceFactory} before a {@link Request} is sent
 * */
public interface RequestInterceptor {

    /**
     * Evaluates a pending request and decides whether to intercept it or not.<br>
     * The pending request may be altered by using its builder.<br>
     * To intercept and abort the request, throw a {@link RequestInterceptedException} or a subtype of it
     * @param pendingRequest The {@link Request.Builder} of the pending request. May be altered
     * @param bodyObject The Object that was used to construct the request body. May be null if no body was specified
     * @param method The method of the resource interface that initiated the request. May be used for inspection
     * @param params The parameters the request method was called with. May be null if Method has no parameters
     * @throws RequestInterceptedException to abort the pending request. This exception will be thrown by the resource method that initiated the request
     * */
    void intercept(@NotNull Request.Builder pendingRequest, @Nullable Object bodyObject, @NotNull Method method, @Nullable Object[] params) throws RequestInterceptedException;

}
