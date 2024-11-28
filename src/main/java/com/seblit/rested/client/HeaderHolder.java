package com.seblit.rested.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for types that support headers
 */
public class HeaderHolder {

    final Map<String, List<String>> headers = new HashMap<>();

    HeaderHolder(@Nullable Map<String, List<String>> headers) {
        setHeaders(headers);
    }

    /**
     * @return an array containing all headers of this object
     */
    public final String @NotNull [] getHeaders() {
        return headers.keySet().toArray(new String[0]);
    }

    /**
     * @param header The desired header
     * @return true if this object contains the requested header
     */
    public final boolean hasHeader(@Nullable String header) {
        return headers.containsKey(header);
    }

    /**
     * @param header The desired header
     * @return an array containing all values that this object holds for the requested header. May be null if the header isn't present
     */
    public final String @Nullable [] getHeaderValues(@Nullable String header) {
        return hasHeader(header) ? headers.get(header).toArray(new String[0]) : null;
    }

    final void setHeaders(@Nullable Map<String, @Nullable List<String>> headers) {
        this.headers.clear();
        if (headers != null) {
            headers.forEach((name, values) -> {
                if (name != null && values != null) {
                    this.headers.put(name, new ArrayList<>(values));
                }
            });
        }
    }


}
