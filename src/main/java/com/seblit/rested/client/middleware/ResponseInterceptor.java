package com.seblit.rested.client.middleware;

import com.seblit.rested.client.Request;
import com.seblit.rested.client.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Notified by a {@link com.seblit.rested.client.ResourceFactory ResourceFactory} after a {@link Response} was received and before the request method returns
 * */
public interface ResponseInterceptor {

    /**
     * Evaluates a received response before the resource method returns and decides whether to intercept it or not.<br>
     * The pending response may be altered by accessing the parsedResponse.<br>
     * To intercept and abort the request, throw a {@link ResponseInterceptedException} or a subtype of it
     * @param request The {@link Request} that lead to the pending response
     * @param response The {@link Response} that was received. Note that response parsing has already been performed and {@link Response#getBodyStream()} may not be accessible anymore. Access parsedResponse instead
     * @param parsedResponse The Object that the response body was parsed into. May be null if no response body was present
     * @param method The method of the resource interface that initiated the request. May be used for inspection
     * @param params The parameters the request method was called with. May be null if Method has no parameters
     * @throws ResponseInterceptedException to abort the pending request. This exception will be thrown by the resource method that initiated the request
     * */
    void intercept(@NotNull Request request, @NotNull Response response, @Nullable Object parsedResponse, @NotNull Method method, @Nullable Object[] params) throws ResponseInterceptedException;

}
