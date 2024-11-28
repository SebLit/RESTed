package com.seblit.rested.client.url;

import com.seblit.rested.client.HTTPClient;
import com.seblit.rested.client.Request;
import com.seblit.rested.client.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Uses a {@link HttpURLConnection} to perform its requests.<br>
 * See {@link HTTPClient} for further information
 * */
public class HttpUrlClient implements HTTPClient {

    private static final String QUERY_PARAM_DELIMITER = "&";
    private static final char QUERY_KEY_VALUE_DELIMITER = '=';
    private static final String PROTOCOL = "http";
    private final String host;
    private final int port;

    /**
     * Creates a new instance with port 80 and the provided host
     * @param host The host this client connects to
     * */
    public HttpUrlClient(@NotNull String host) {
        this(host, 80);
    }

    /**
     * Creates a new instance with the provided port and host
     * @param host The host this client connects to
     * @param port The port this client connects to
     * */
    public HttpUrlClient(@NotNull String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * @return the host this client connects to
     * */
    @NotNull
    public String getHost() {
        return host;
    }

    /**
     * @return the port this client connects to
     * */
    public int getPort() {
        return port;
    }

    /**
     * {@inheritDoc}
     * @throws Exception any that may occur during the request
     * */
    @Override
    public @NotNull Response request(@NotNull Request request, @NotNull Method method, @Nullable Object[] params) throws Exception {
        String encodedQuery = buildQuery(request);
        boolean hasBody = request.getBody() != null;
        boolean mayHaveResponseBody = request.getMethod().isResponseBodySupported();
        HttpURLConnection connection = createConnection(request.getPath(), encodedQuery);
        connection.setRequestMethod(request.getMethod().name());
        connection.setDoOutput(hasBody);
        connection.setDoInput(mayHaveResponseBody);
        addHeaders(connection, request);
        if (hasBody) {
            try (OutputStream output = connection.getOutputStream()) {
                output.write(request.getBody());
                output.flush();
            }
        } else {
            connection.connect();
        }
        int statusCode = connection.getResponseCode();
        InputStream bodyStream = getResponseBodyStream(connection, statusCode, mayHaveResponseBody);
        Map<String, List<String>> responseHeaders = new HashMap<>(connection.getHeaderFields());
        responseHeaders.remove(null); // HttpUrlConnection uses key null for response message inside header fields
        return new Response(statusCode, connection.getResponseMessage(), bodyStream, responseHeaders);
    }

    /**
     * Creates the {@link HttpURLConnection} for the requested path and query. The remaining configuration will take place afterward
     * @param path The endpoint path
     * @param query The request query. May be null if none is required
     * @throws Exception any exceptions that may occur during the creation of the connection
     * @return the created {@link HttpURLConnection}
     * */
    @NotNull
    protected HttpURLConnection createConnection(@NotNull String path, @Nullable String query) throws Exception {
        return (HttpURLConnection) new URI(PROTOCOL, null, host, port, path, query, null).toURL().openConnection();
    }

    private InputStream getResponseBodyStream(HttpURLConnection connection, int statusCode, boolean mayHaveResponseBody) throws IOException {
        if (statusCode / 100 != 2) {
            return connection.getErrorStream();
        } else if (mayHaveResponseBody) {
            return connection.getInputStream();
        }
        return null;
    }

    private void addHeaders(HttpURLConnection connection, Request request) throws UnsupportedEncodingException {
        for (String header : request.getHeaders()) {
            for (String headerValue : request.getHeaderValues(header)) {
                connection.addRequestProperty(header, headerValue);
            }
        }
    }

    private String buildQuery(Request request) throws UnsupportedEncodingException {
        StringJoiner queryJoiner = new StringJoiner(QUERY_PARAM_DELIMITER);
        String charset = StandardCharsets.UTF_8.name();
        for (String queryParam : request.getQueryParams()) {
            String encodedParam = URLEncoder.encode(queryParam,  charset);
            for (String queryValue : request.getQueryParamValues(queryParam)) {
                String encodedValue = URLEncoder.encode(queryValue, charset);
                queryJoiner.add(encodedParam + QUERY_KEY_VALUE_DELIMITER + encodedValue);
            }
        }
        return queryJoiner.length() != 0 ? queryJoiner.toString() : null;
    }


}
