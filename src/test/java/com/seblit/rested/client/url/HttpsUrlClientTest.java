package com.seblit.rested.client.url;

import org.junit.Test;

import javax.net.ssl.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

import static org.mockito.Mockito.*;

public class HttpsUrlClientTest extends HttpUrlClientTest {

    private HostnameVerifier mockedHostVerifier = mock(HostnameVerifier.class);
    private X509TrustManager mockedTrustManager = mock(X509TrustManager.class);
    private HttpsUrlClient httpsClient;

    @Override
    public void setup() throws Exception {
        mockedHostVerifier = mock(HostnameVerifier.class);
        when(mockedHostVerifier.verify(any(), any())).thenReturn(true);

        httpsClient = new HttpsUrlClient(TEST_HOST, TEST_PORT);
        this.client = httpsClient;
        httpsClient.setHostnameVerifier(mockedHostVerifier);
        httpsClient.setSocketFactory(initSocketFactory());
    }

    @Test(expected = SSLHandshakeException.class)
    public void testSocketFactory_byTrustManager() throws Exception {
        doThrow(CertificateException.class).when(mockedTrustManager).checkServerTrusted(any(), any());
        httpsClient.request(mockedSuccessRequest, mock(Method.class), null);
    }

    @Test(expected = IOException.class)
    public void testHostnameVerifier() throws Exception {
        when(mockedHostVerifier.verify(any(), any())).thenReturn(false);
        httpsClient.request(mockedSuccessRequest, mock(Method.class), null);
    }

    private SSLSocketFactory initSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[]{mockedTrustManager}, new SecureRandom());
        return context.getSocketFactory();
    }

}
