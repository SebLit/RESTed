package com.seblit.rested.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents a request that can be executed by a {@link HTTPClient}. Use {@link Request.Builder} to create one
 */
public class Request extends HeaderHolder {

    private final RequestMethod method;
    private final String path;
    private final byte[] body;
    private final Map<String, List<String>> queryParams = new HashMap<>();

    private Request(@NotNull RequestMethod method, @Nullable String path, byte @Nullable [] body, @Nullable Map<String, List<String>> headers, @Nullable Map<String, List<String>> queryParams) {
        super(headers);
        this.method = method;
        this.path = path;
        this.body = body != null ? Arrays.copyOf(body, body.length) : null;
        if (queryParams != null) {
            queryParams.forEach((name, values) -> this.queryParams.put(name, new ArrayList<>(values)));
        }
    }

    /**
     * @return the {@link RequestMethod} of this request
     */
    @NotNull
    public RequestMethod getMethod() {
        return method;
    }

    /**
     * @return the endpoint path of this request. May be null if the base path of the {@link com.seblit.rested.client.annotation.Resource Resource} is used
     */
    @Nullable
    public String getPath() {
        return path;
    }

    /**
     * @return the body of this request. May be null if no body is set
     */
    public byte @Nullable [] getBody() {
        return body != null ? Arrays.copyOf(body, body.length) : null;
    }

    /**
     * @return an array containing all query parameters of this request
     */
    public String @NotNull [] getQueryParams() {
        return queryParams.keySet().toArray(new String[0]);
    }

    /**
     * @param queryParam The desired query parameter
     * @return true if this request contains the provided query parameter
     */
    public boolean hasQueryParam(@Nullable String queryParam) {
        return queryParams.containsKey(queryParam);
    }

    /**
     * @param queryParam The desired query parameter
     * @return an array containing all values for the provided query parameter. May be null if the query parameter isn't present
     */
    @Nullable
    public String[] getQueryParamValues(String queryParam) {
        return hasQueryParam(queryParam) ? queryParams.get(queryParam).toArray(new String[0]) : null;
    }

    /**
     * A Builder to construct a {@link Request}
     */
    public static class Builder extends HeaderHolder {

        private RequestMethod method = RequestMethod.GET;
        private String path = "";
        private byte[] body;
        private final Map<String, List<String>> queryParams = new HashMap<>();

        public Builder() {
            super(null);
        }

        /**
         * Creates a new {@link Request} with the current builder values
         *
         * @return the created {@link Request}
         */
        @NotNull
        public Request build() {
            return new Request(method, path, body, headers, queryParams);
        }

        /**
         * Sets the {@link RequestMethod} of this builder
         *
         * @param method The {@link RequestMethod}
         * @return the builder instance for method chaining
         * @throws IllegalArgumentException if method is null
         */
        @NotNull
        public Builder setMethod(@NotNull RequestMethod method) {
            if (method == null) {
                throw new IllegalArgumentException("method may not be null. All requests require it");
            }
            this.method = method;
            return this;
        }

        /**
         * @return the current {@link RequestMethod} of this builder. Default: {@link RequestMethod#GET}
         */
        @NotNull
        public RequestMethod getMethod() {
            return method;
        }

        /**
         * Sets the path of this builder.
         *
         * @param path The path. May be null for no path
         * @return the builder instance for method chaining
         */
        @NotNull
        public Builder setPath(@Nullable String path) {
            this.path = path;
            return this;
        }

        /**
         * @return the current path of this builder. May be null
         */
        @Nullable
        public String getPath() {
            return path;
        }

        /**
         * Sets the body of this builder
         *
         * @param body The body. May be null for no body
         * @return the builder instance for method chaining
         */
        @NotNull
        public Builder setBody(byte @Nullable [] body) {
            this.body = body;
            return this;
        }

        /**
         * @return the current body of this builder. May be null
         */
        public byte @Nullable [] getBody() {
            return body;
        }

        /**
         * Adds a key-value pair to the headers of this builder. If the header is already present, the value will be appended to it
         *
         * @param header The header name. null values will be ignored
         * @param value  The header value. null values will be ignored
         * @return the builder instance for method chaining
         */
        @NotNull
        public Builder addHeader(@Nullable String header, @Nullable String value) {
            if (header != null && value != null) {
                headers.computeIfAbsent(header, s -> new ArrayList<>()).add(value);
            }
            return this;
        }

        /**
         * Removes the entire header from this builder
         *
         * @param header The header name
         * @return the builder instance for method chaining
         */
        @NotNull
        public Builder removeHeader(@Nullable String header) {
            headers.remove(header);
            return this;
        }

        /**
         * Removes a specific value of the desired header from this builder. If the value isn't present, the call is ignored
         *
         * @param header The header name. null values will be ignored
         * @param value  The header value to remove. null values will be ignored
         * @return the builder instance for method chaining
         */
        @NotNull
        public Builder removeHeader(@Nullable String header, @Nullable String value) {
            if (headers.containsKey(header)) {
                headers.get(header).remove(value);
                if (headers.get(header).isEmpty()) {
                    headers.remove(header);
                }
            }
            return this;
        }

        /**
         * Adds a key-value pair to the query parameters of this builder. If the parameter is already present, the value will be appended to it
         *
         * @param queryParam The parameter name. null values will be ignored
         * @param value      The parameter value. null values will be ignored
         * @return the builder instance for method chaining
         */
        @NotNull
        public Builder addQueryParam(@Nullable String queryParam, @Nullable String value) {
            if (queryParam != null && value != null) {
                queryParams.computeIfAbsent(queryParam, s -> new ArrayList<>()).add(value);
            }
            return this;
        }

        /**
         * Removes the entire query parameter from this builder
         *
         * @param queryParam The parameter name
         * @return the builder instance for method chaining
         */
        @NotNull
        public Builder removeQueryParam(@Nullable String queryParam) {
            queryParams.remove(queryParam);
            return this;
        }

        /**
         * Removes a specific value of the desired query parameter from this builder. If the value isn't present, the call is ignored
         *
         * @param queryParam The parameter name. null values will be ignored
         * @param value      The parameter value to remove. null values will be ignored
         * @return the builder instance for method chaining
         */
        @NotNull
        public Builder removeQueryParam(@Nullable String queryParam, @Nullable String value) {
            if (hasQueryParam(queryParam)) {
                queryParams.get(queryParam).remove(value);
                if (queryParams.get(queryParam).isEmpty()) {
                    queryParams.remove(queryParam);
                }
            }
            return this;
        }

        /**
         * @return true if this builder contains the requested query parameter
         */
        public boolean hasQueryParam(@Nullable String queryParam) {
            return queryParams.containsKey(queryParam);
        }

        /**
         * @return an array containing all current query parameters of this builder
         */
        public String @NotNull [] getQueryParams() {
            return queryParams.keySet().toArray(new String[0]);
        }

        /**
         * @param queryParam The desired parameter
         * @return an array containing all values that this builder currently holds for the requested query parameter. May be null if the parameter isn't present
         */
        public String @Nullable [] getQueryParamValues(@Nullable String queryParam) {
            return hasQueryParam(queryParam) ? queryParams.get(queryParam).toArray(new String[0]) : null;
        }
    }

}
