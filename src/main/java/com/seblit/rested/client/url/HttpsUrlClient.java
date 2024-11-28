package com.seblit.rested.client.url;

import com.seblit.rested.client.HTTPClient;
import com.seblit.rested.client.Request;
import com.seblit.rested.client.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;

/**
 * Based on {@link HttpUrlClient} but uses a {@link HttpsURLConnection} instead.<br>
 * See {@link HTTPClient} for further information
 * */
public class HttpsUrlClient extends HttpUrlClient {

    private static final String PROTOCOL = "https";

    private HostnameVerifier hostnameVerifier;
    private SSLSocketFactory factory;

    /**
     * Creates a new instance with provided port and host
     * @param host The host this client connects to
     * @param port The port this client connects to
     * */
    public HttpsUrlClient(@NotNull String host, int port) {
        super(host, port);
    }

    /**
     * Creates a new instance with port 443 and the provided host
     * @param host The host this client connects to
     * */
    public HttpsUrlClient(@NotNull String host) {
        super(host, 443);
    }

    /**
     * Sets the {@link HostnameVerifier} that will be used by the underlying {@link HttpsURLConnection}
     * @param hostnameVerifier The verifier to use. null to use default
     * */
    public void setHostnameVerifier(@Nullable HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    /**
     * Sets the {@link SSLSocketFactory} that will be used by the underlying {@link HttpsURLConnection}
     * @param factory The factory to use. null to use default
     * */
    public void setSocketFactory(@Nullable SSLSocketFactory factory) {
        this.factory = factory;
    }

    /**
     * Creates a {@link HttpsURLConnection} with the currently configured {@link HostnameVerifier} and {@link SSLSocketFactory}, if set.
     * {@inheritDoc}
     * @return the created {@link HttpsURLConnection}
     * */
    @Override
    @NotNull
    protected HttpURLConnection createConnection(@NotNull String path, @Nullable String query) throws Exception {
        HttpsURLConnection connection = (HttpsURLConnection) new URI(PROTOCOL, null, getHost(), getPort(), path, query, null).toURL().openConnection();
        if (hostnameVerifier != null) {
            connection.setHostnameVerifier(hostnameVerifier);
        }
        if (factory != null) {
            connection.setSSLSocketFactory(factory);
        }
        return connection;
    }
}
