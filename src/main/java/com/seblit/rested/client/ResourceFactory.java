package com.seblit.rested.client;

import com.seblit.rested.client.annotation.Error;
import com.seblit.rested.client.annotation.*;
import com.seblit.rested.client.media.MissingRequestParserException;
import com.seblit.rested.client.media.MissingResponseParserException;
import com.seblit.rested.client.media.RequestBodyParser;
import com.seblit.rested.client.media.ResponseBodyParser;
import com.seblit.rested.client.middleware.RequestInterceptor;
import com.seblit.rested.client.middleware.ResponseInterceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A factory that creates resource instances from interfaces that declare {@link Endpoint}s.<br>
 * Calls to these instances will result in a {@link Request} according to the methods definition being executed by the factories {@link HTTPClient}.<br>
 * To transform between binary data and objects the factory uses {@link RequestBodyParser} and {@link ResponseBodyParser}.<br>
 * {@link RequestInterceptor} and {@link ResponseInterceptor} may be used to inspect, alter and abort pending requests and responses, before they are processed.<br>
 * To declare endpoints in an interface, use the {@link Endpoint}, {@link Header}, {@link Body}, {@link PathParam}, {@link QueryParam}, {@link Error} and {@link Resource} annotations
 * on its methods and their parameters. For further details, see the documentation of each annotation.<br>
 * To include http status code and headers into a response, extend from {@link RESTResponse} or for exceptions from {@link RESTException} for your return type.
 * To stream the response body instead of parsing it, use {@link StreamedRESTResponse} or for exceptions {@link StreamedRESTException} for your return type.
 */
public class ResourceFactory {

    private final HTTPClient client;
    private final ResourceHandler handler = new ResourceHandler();
    private final Map<String, RequestBodyParser> requestParserRegistry = new HashMap<>();
    private final Map<String, ResponseBodyParser> responseParserRegistry = new HashMap<>();
    private final List<RequestInterceptor> requestInterceptors = new ArrayList<>();
    private final List<ResponseInterceptor> responseInterceptors = new ArrayList<>();

    /**
     * Creates a new instance
     *
     * @param client The {@link HTTPClient} this factory uses for requests
     */
    public ResourceFactory(@NotNull HTTPClient client) {
        this.client = client;
    }

    /**
     * Registers the provided {@link RequestBodyParser} for all provided media types. If other parsers have already been registered
     * for the same media type, they will be replaced with the new one. They are used to transform the request body into binary data.<br>
     * The media types may contain wildcards to register a parser for all subtypes or even any type. If there are parsers available for both,
     * an explicit media type and wildcard types that include it, the explicit one is used.<br>
     * I.e. parsers with application/json would be used only for JSON, application/&#42; would be used for all application media types and &#42;/&#42; would be
     * used for any media type. But for parsing the media type application/json, the explicit parser would be considered first, then the application type parser
     * and last the generic parser
     *
     * @param parser     The parser that should be registered
     * @param mediaTypes The media types the parser should be used for. May include wildcard types
     */
    public void registerRequestParser(@NotNull RequestBodyParser parser, String @NotNull ... mediaTypes) {
        synchronized (requestParserRegistry) {
            for (String mediaType : mediaTypes) {
                if (mediaType != null) {
                    requestParserRegistry.put(mediaType, parser);
                }
            }
        }
    }

    /**
     * Removes the {@link RequestBodyParser}s that were registered for the provided media types
     *
     * @param mediaTypes The media types which will not be supported by this factory after this call
     */
    public void unregisterRequestParser(String @NotNull ... mediaTypes) {
        synchronized (requestParserRegistry) {
            for (String mediaType : mediaTypes) {
                requestParserRegistry.remove(mediaType);
            }
        }
    }

    /**
     * Registers the provided {@link ResponseBodyParser} for all provided media types. If other parsers have already been registered
     * for the same media type, they will be replaced with the new one. They are used to transform the response binary body into objects.<br>
     * The media types may contain wildcards to register a parser for all subtypes or even any type. If there are parsers available for both,
     * an explicit media type and wildcard types that include it, the explicit one is used.<br>
     * I.e. parsers with application/json would be used only for JSON, application/&#42; would be used for all application media types and &#42;/&#42; would be
     * used for any media type. But for parsing the media type application/json, the explicit parser would be considered first, then the application type parser
     * and last the generic parser
     *
     * @param parser     The parser that should be registered
     * @param mediaTypes The media types the parser should be used for. May include wildcard types
     */
    public void registerResponseParser(@NotNull ResponseBodyParser parser, String @NotNull ... mediaTypes) {
        synchronized (responseParserRegistry) {
            for (String mediaType : mediaTypes) {
                if (mediaType != null) {
                    responseParserRegistry.put(mediaType, parser);
                }
            }
        }
    }

    /**
     * Removes the {@link ResponseBodyParser}s that were registered for the provided media types
     *
     * @param mediaTypes The media types which will not be supported by this factory after this call
     */
    public void unregisterResponseParser(String @NotNull ... mediaTypes) {
        synchronized (responseParserRegistry) {
            for (String mediaType : mediaTypes) {
                responseParserRegistry.remove(mediaType);
            }
        }
    }

    /**
     * Adds the provided {@link RequestInterceptor}s to this factory. It will be notified for each future request before it is executed
     *
     * @param interceptors The interceptors to add.
     */
    public void addRequestInterceptors(RequestInterceptor @NotNull ... interceptors) {
        synchronized (requestInterceptors) {
            requestInterceptors.addAll(Arrays.stream(interceptors).filter(Objects::nonNull).collect(Collectors.toList()));
        }
    }

    /**
     * Removes all provided {@link RequestInterceptor}s from this factory
     *
     * @param interceptors The interceptors to remove
     */
    public void removeRequestInterceptors(RequestInterceptor @NotNull ... interceptors) {
        synchronized (requestInterceptors) {
            Arrays.stream(interceptors).forEach(requestInterceptors::remove);
        }
    }

    /**
     * Adds the provided {@link ResponseInterceptor}s to this factory. It will be notified for each future response that was received
     * before the resource method returns
     *
     * @param interceptors The interceptors to add.
     */
    public void addResponseInterceptors(ResponseInterceptor @NotNull ... interceptors) {
        synchronized (responseInterceptors) {
            responseInterceptors.addAll(Arrays.stream(interceptors).filter(Objects::nonNull).collect(Collectors.toList()));
        }
    }

    /**
     * Removes all provided {@link ResponseInterceptor}s from this factory
     *
     * @param interceptors The interceptors to remove
     */
    public void removeResponseInterceptors(ResponseInterceptor @NotNull ... interceptors) {
        synchronized (responseInterceptors) {
            Arrays.stream(interceptors).forEach(responseInterceptors::remove);
        }
    }

    /**
     * Creates a {@link Proxy} instance for the provided interface type.<br>
     * Method calls to this instance will result in a http request according to its configuration
     * <h1>Request configuration</h1>
     * <li>{@link Endpoint} must be used to declare the method as a REST endpoint</li>
     * <li>Use {@link Header} on a parameter to declare it as an additional request header. null values will be ignored</li>
     * <li>Use {@link Body} on a parameter to declare it as the request body. Only one annotation per request allowed</li>
     * <li>Use {@link QueryParam} on a parameter to declare it as a query parameter. null values will be ignored</li>
     * <li>Use {@link Resource} on the interface class to declare a base path for all endpoints</li>
     * <li>You may declare path parameters in the resource and endpoint path by wrapping them in {parentheses}. Use @{@link PathParam}
     * on a parameter to declare it as the replace value for the path parameter</li>
     * <li>Use {@link Error} on the method to declare custom error types for specific response code ranges</li>
     * <li>Methods should be declared with <code>throws Exception</code> so {@link Exception}s that are thrown during execution
     * do not get wrapped in a {@link UndeclaredThrowableException}</li>
     * <li>Any {@link Throwable}s that may be thrown by any of the steps of the request will be thrown by its method.</li>
     *
     * <h1>Request steps</h1>
     * <li>Request construction: A {@link Request.Builder} will be created with values according to the invoked request method. After this
     * the Accept and Accept-Charset headers will be added, if not already manually set.
     * If a body is specified, the {@link RequestBodyParser} for its media type will be invoked to create the binary data for the request body.
     * After this the Content-Type and Content-Length headers will be added, if not already manually set.
     * May result in a {@link MissingRequestParserException} if no parser is registered for the required media type. May also result in any Exception that the
     * parser implementation may produce during {@link RequestBodyParser#parse(Object, String, String)}</li>
     * <li>Request interception: All available {@link RequestInterceptor}s are notified about the pending request. May result in a {@link com.seblit.rested.client.middleware.RequestInterceptedException RequestInterceptedException}
     * if an interceptor decides to abort the request</li>
     * <li>Request execution: The final {@link Request} is constructed and the factories {@link HTTPClient} is called to execute it.
     * May result in any Exceptions that the client implementation may produce during {@link HTTPClient#request(Request, Method, Object[])}</li>
     * <li>Response parsing:<br>First the desired return type is determined. For success responses (status code 2xx) this is the return type of the method (void will skip response parsing).
     * For error responses this is the Exception type for the status code declared by {@link Error} or default {@link RESTException}.<br><br>
     * Then, unless the type is an instance of {@link StreamedRESTResponse} or {@link StreamedRESTException}, the {@link ResponseBodyParser} for the response media type will be invoked
     * to parse the binary response body into the desired object type. After parsing the stream will be closed. May result in a {@link MissingResponseParserException} if no parser is registered for the required media type.
     * May also result in any Exception that the parser implementation may produce during {@link ResponseBodyParser#parse(Class, Request, Response, String, String)}</li>
     * <li>Response interception: All available {@link ResponseInterceptor}s are notified about the pending response. May result in a {@link com.seblit.rested.client.middleware.ResponseInterceptedException ResponseInterceptedException}
     * if an interceptor decides to abort the response</li>
     * <li>Response delivery: A successful response will result in a return of the request method with the declared return type containing the response data.
     * An error response will cause the created exception (see Response parsing) to be thrown by the request method.</li>
     */
    @NotNull
    public <R> R createResource(@NotNull Class<R> type) {
        return (R) Proxy.newProxyInstance(ResourceFactory.class.getClassLoader(), new Class[]{type}, handler);
    }

    private class ResourceHandler implements InvocationHandler {

        private static final String FORMAT_PATH_PARAM = "{%s}";
        private static final String FORMAT_HEADER_CONTENT_TYPE = "%s; charset=%s";
        private static final String HEADER_ACCEPT = "Accept";
        private static final String HEADER_ACCEPT_DELIMITER = ", ";
        private static final String HEADER_ACCEPT_CHARSET = "Accept-Charset";
        private static final String HEADER_CONTENT_TYPE = "Content-Type";
        private static final String HEADER_CONTENT_LENGTH = "Content-Length";
        private static final String MEDIA_TYPE_GENERIC = "*/*";
        private static final String MEDIA_SUBTYPE_SEPARATOR = "/";
        private static final String GENERIC_MEDIA = "*";
        private static final String CONTENT_TYPE_CHARSET_PREFIX = "charset=";
        private static final String CONTENT_TYPE_CHARSET_SEPARATOR = ";";

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Endpoint endpoint = method.getDeclaredAnnotation(Endpoint.class);
            Request.Builder requestBuilder = new Request.Builder()
                    .setMethod(endpoint.value())
                    .setPath(buildPath(endpoint, method, args));
            loadHeaders(endpoint, requestBuilder, method, args);
            loadQuery(requestBuilder, method, args);
            Object bodyObject = loadBody(requestBuilder, method, args);

            synchronized (requestInterceptors) {
                for (RequestInterceptor interceptor : requestInterceptors) {
                    interceptor.intercept(requestBuilder, bodyObject, method, args);
                }
            }
            Request request = requestBuilder.build();
            Response response = client.request(request, method, args);
            Object parsedResponse = parseResponse(request, response, method);

            synchronized (responseInterceptors) {
                for (ResponseInterceptor interceptor : responseInterceptors) {
                    interceptor.intercept(request, response, parsedResponse, method, args);
                }
            }

            if (parsedResponse instanceof Throwable) {
                throw (Throwable) parsedResponse;
            }
            return parsedResponse;
        }

        private Object parseResponse(Request request, Response response, Method method) throws Exception {
            InputStream bodyStream = response.getBodyStream();
            try {
                Class<?> resultType;
                if (response.isSuccessResponse()) {
                    resultType = method.getReturnType();
                    if (!RESTResponse.class.isAssignableFrom(resultType) && (void.class == resultType || bodyStream == null)) {
                        return null;
                    }
                } else {
                    resultType = loadErrorType(response.getStatusCode(), method);
                }
                Object result;
                if (StreamedRESTResponse.class == resultType) {
                    result = new StreamedRESTResponse(bodyStream);
                    bodyStream = null;
                } else if (StreamedRESTException.class.isAssignableFrom(resultType)) {
                    StreamedRESTException error = (StreamedRESTException) resultType.getConstructor().newInstance();
                    error.init(bodyStream);
                    bodyStream = null;
                    result = error;
                } else {
                    Map.Entry<String, String> mediaInfo = parseMediaInfo(response);
                    ResponseBodyParser parser = findParser(responseParserRegistry, mediaInfo.getKey(), true);
                    result = parser.parse(resultType, request, response, mediaInfo.getKey(), mediaInfo.getValue());
                }
                if (result instanceof RESTResponse) {
                    ((RESTResponse) result).init(response.getStatusCode(), response.getMessage(), response.headers);
                } else if (result instanceof RESTException) {
                    ((RESTException) result).init(response.getStatusCode(), response.getMessage(), response.headers, request);
                }
                return result;
            } finally {
                if (bodyStream != null) {
                    bodyStream.close();
                }
            }
        }

        private Map.Entry<String, String> parseMediaInfo(HeaderHolder headerHolder) {
            if (headerHolder.hasHeader(HEADER_CONTENT_TYPE)) {
                String contentType = headerHolder.getHeaderValues(HEADER_CONTENT_TYPE)[0];
                String[] mediaInfo = contentType.split(CONTENT_TYPE_CHARSET_SEPARATOR, 2);
                String mediaType = mediaInfo[0].trim();
                String charset = Charset.defaultCharset().name();
                if (mediaInfo.length == 2) {
                    String formattedCharset = mediaInfo[1];
                    charset = formattedCharset.substring(formattedCharset.indexOf(CONTENT_TYPE_CHARSET_PREFIX) + CONTENT_TYPE_CHARSET_PREFIX.length()).trim();
                }
                return new AbstractMap.SimpleEntry<>(mediaType, charset);
            }
            return new AbstractMap.SimpleEntry<>(MEDIA_TYPE_GENERIC, Charset.defaultCharset().name());
        }

        private Class<? extends Throwable> loadErrorType(int responseCode, Method method) {
            Class<? extends Throwable> errorType = RESTException.class;
            Errors errors = method.getDeclaredAnnotation(Errors.class);
            Error[] declaredErrors;
            if (errors != null) {
                declaredErrors = errors.value();
            } else {
                declaredErrors = new Error[]{method.getDeclaredAnnotation(Error.class)};
            }
            for (Error error : declaredErrors) {
                if (error != null && error.startCode() <= responseCode && error.endCode() >= responseCode) {
                    errorType = error.value();
                    break;
                }
            }
            return errorType;
        }

        private <P> P findParser(Map<String, P> parserRegistry, String mediaType, boolean isResponse) {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (parserRegistry) {
                P parser = parserRegistry.get(mediaType);
                if (parser == null && mediaType.contains(MEDIA_SUBTYPE_SEPARATOR)) {
                    String parentType = mediaType.substring(0, mediaType.indexOf(MEDIA_SUBTYPE_SEPARATOR) + MEDIA_SUBTYPE_SEPARATOR.length()) + GENERIC_MEDIA;
                    parser = parserRegistry.get(parentType);
                }
                if (parser == null) {
                    parser = parserRegistry.get(MEDIA_TYPE_GENERIC);
                }
                if (parser == null) {
                    throw isResponse ? new MissingResponseParserException(mediaType) : new MissingRequestParserException(mediaType);
                }
                return parser;
            }
        }

        private void loadHeaders(Endpoint endpoint, Request.Builder builder, Method method, Object[] args) {
            Parameter[] params = method.getParameters();
            for (int paramIndex = 0; paramIndex < params.length; paramIndex++) {
                Header header = params[paramIndex].getDeclaredAnnotation(Header.class);
                if (header != null) {
                    forEachParamValue(args[paramIndex], value -> builder.addHeader(header.value(), String.valueOf(value)));
                }
            }
            if (!builder.hasHeader(HEADER_ACCEPT) && endpoint.mediaTypes().length != 0) {
                builder.addHeader(HEADER_ACCEPT, String.join(HEADER_ACCEPT_DELIMITER, endpoint.mediaTypes()));
            }
            if (!builder.hasHeader(HEADER_ACCEPT_CHARSET) && endpoint.charsets().length != 0) {
                builder.addHeader(HEADER_ACCEPT_CHARSET, String.join(HEADER_ACCEPT_DELIMITER, endpoint.charsets()));
            }
        }

        private void loadQuery(Request.Builder builder, Method method, Object[] args) {
            Parameter[] params = method.getParameters();
            for (int paramIndex = 0; paramIndex < params.length; paramIndex++) {
                QueryParam queryParam = params[paramIndex].getDeclaredAnnotation(QueryParam.class);
                if (queryParam != null) {
                    forEachParamValue(args[paramIndex], value -> builder.addQueryParam(queryParam.value(), String.valueOf(value)));
                }
            }
        }

        private void forEachParamValue(Object param, Consumer<Object> consumer) {
            if (param != null) {
                if (param.getClass().isArray()) {
                    param = Arrays.asList((Object[]) param);
                }
                if (param instanceof Iterable<?>) {
                    ((Iterable<?>) param).forEach(value -> {
                        if (value != null) {
                            consumer.accept(value);
                        }
                    });
                } else {
                    consumer.accept(param);
                }
            }
        }

        private Object loadBody(Request.Builder builder, Method method, Object[] args) throws Exception {
            Parameter[] params = method.getParameters();
            for (int paramIndex = 0; paramIndex < params.length; paramIndex++) {
                Body body = params[paramIndex].getDeclaredAnnotation(Body.class);
                if (body != null) {
                    Object bodyObject = args[paramIndex];
                    if (bodyObject != null) {
                        RequestBodyParser parser = findParser(requestParserRegistry, body.value(), false);
                        byte[] bodyData = parser.parse(bodyObject, body.value(), body.charset());
                        if (bodyData != null) {
                            if (!builder.hasHeader(HEADER_CONTENT_TYPE)) {
                                builder.addHeader(HEADER_CONTENT_TYPE, String.format(FORMAT_HEADER_CONTENT_TYPE, body.value(), body.charset()));
                            }
                            if (!builder.hasHeader(HEADER_CONTENT_LENGTH)) {
                                builder.addHeader(HEADER_CONTENT_LENGTH, Integer.toString(bodyData.length));
                            }
                            builder.setBody(bodyData);
                        }
                    }
                    return bodyObject;
                }
            }
            return null;
        }

        private String buildPath(Endpoint endpoint, Method method, Object[] args) {
            Resource resource = method.getDeclaringClass().getDeclaredAnnotation(Resource.class);
            String resourcePath = resource != null ? resource.value() : "";
            String path = resourcePath + endpoint.path();
            Parameter[] params = method.getParameters();
            for (int paramIndex = 0; paramIndex < params.length; paramIndex++) {
                PathParam param = params[paramIndex].getDeclaredAnnotation(PathParam.class);
                if (param != null) {
                    Object paramValue = args[paramIndex];
                    if (paramValue != null) {
                        path = path.replaceAll(Pattern.quote(String.format(FORMAT_PATH_PARAM, param.value())), String.valueOf(paramValue));
                    }
                }
            }
            return path;
        }

    }

}
