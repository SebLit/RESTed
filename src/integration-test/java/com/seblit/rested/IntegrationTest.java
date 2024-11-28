package com.seblit.rested;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.stream.JsonWriter;
import com.seblit.rested.client.*;
import com.seblit.rested.client.annotation.*;
import com.seblit.rested.client.annotation.Error;
import com.seblit.rested.client.media.RequestBodyParser;
import com.seblit.rested.client.media.ResponseBodyParser;
import com.seblit.rested.client.middleware.RequestInterceptor;
import com.seblit.rested.client.middleware.ResponseInterceptor;
import com.seblit.rested.client.url.HttpsUrlClient;
import org.junit.After;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import javax.net.ssl.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

public class IntegrationTest {

    private static final String HOST = "localhost";
    private static final int PORT = 443;

    private TestResource resource;

    private RequestBodyParser mockedRequestParser = mock(RequestBodyParser.class);
    private ResponseBodyParser mockedResponseParser = mock(ResponseBodyParser.class);
    private RequestInterceptor mockedRequestInterceptor = mock(RequestInterceptor.class);
    private ResponseInterceptor mockedResponseInterceptor = mock(ResponseInterceptor.class);
    private X509TrustManager mockedTrustManager = mock(X509TrustManager.class);
    private HostnameVerifier mockedHostVerifier = mock(HostnameVerifier.class);

    private byte[] lastRequestParseResult;
    private Object lastResponseParseResult;

    private ClientAndServer mockedServer;
    private HttpRequest successRequest;

    @Before
    public void setup() throws Exception {
        when(mockedHostVerifier.verify(any(), any())).thenReturn(true);
        mockJSONParsers();
        HttpsUrlClient client = new HttpsUrlClient(HOST, PORT);
        client.setSocketFactory(createSocketFactory());
        client.setHostnameVerifier(mockedHostVerifier);
        ResourceFactory factory = new ResourceFactory(client);
        factory.registerRequestParser(mockedRequestParser, "application/json");
        factory.registerResponseParser(mockedResponseParser, "application/json");
        factory.addRequestInterceptors(mockedRequestInterceptor);
        factory.addResponseInterceptors(mockedResponseInterceptor);
        resource = factory.createResource(TestResource.class);
        mockedServer = ClientAndServer.startClientAndServer(PORT);
        mockServer();
    }


    @After
    public void stopServer() {
        mockedServer.stop();
    }

    @Test
    public void test_SuccessRequest() throws Exception {
        RequestObject requestBody = new RequestObject(10, 5.6f);
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        ArgumentCaptor<Request.Builder> requestBuilderCaptor = ArgumentCaptor.forClass(Request.Builder.class);
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        ResponseObject result = resource.requestSuccess("headerValue", "success", "queryParamValue", requestBody);
        // verify parsers
        verify(mockedRequestParser).parse(requestBody, "application/json", "UTF-8");
        verify(mockedResponseParser).parse(same(ResponseObject.class), requestCaptor.capture(), responseCaptor.capture(), eq("application/json"), eq("UTF-8"));
        assertSame(result, lastResponseParseResult);
        // verify request
        Request capturedRequest = requestCaptor.getValue();
        assertEquals(RequestMethod.POST, capturedRequest.getMethod());
        assertEquals("/resource/endpoint/success", capturedRequest.getPath());
        assertArrayEquals(new String[]{"headerValue"}, capturedRequest.getHeaderValues("header"));
        assertArrayEquals(new String[]{"application/json; charset=UTF-8"}, capturedRequest.getHeaderValues("Content-Type"));
        // verify response
        Response capturedResponse = responseCaptor.getValue();
        assertEquals(200, capturedResponse.getStatusCode());
        assertEquals("OK", capturedResponse.getMessage());
        assertNotNull(capturedResponse.getBodyStream());
        assertArrayEquals(new String[]{"headerValue"}, capturedResponse.getHeaderValues("header"));
        assertArrayEquals(new String[]{"application/json; charset=UTF-8"}, capturedResponse.getHeaderValues("Content-Type"));
        // verify interceptors
        verify(mockedRequestInterceptor).intercept(requestBuilderCaptor.capture(), same(requestBody), any(), any());
        verify(mockedResponseInterceptor).intercept(same(capturedRequest), same(capturedResponse), same(result), any(), any());
        Request.Builder capturedbuilder = requestBuilderCaptor.getValue();
        assertEquals(RequestMethod.POST, capturedbuilder.getMethod());
        assertEquals("/resource/endpoint/success", capturedbuilder.getPath());
        assertArrayEquals(new String[]{"headerValue"}, capturedbuilder.getHeaderValues("header"));
        assertArrayEquals(new String[]{"application/json; charset=UTF-8"}, capturedbuilder.getHeaderValues("Content-Type"));
        // verify connection
        verify(mockedTrustManager).checkServerTrusted(any(), any());
        verify(mockedHostVerifier).verify(any(), any());
        mockedServer.verify(successRequest);
        // verify result
        assertEquals("some name", result.name);
        assertEquals(5, result.age);
        assertEquals(3.5f, result.size, 0.0f);
        assertEquals(200, result.getStatusCode());
        assertEquals("OK", result.getResponseMessage());
        assertArrayEquals(new String[]{"headerValue"}, result.getHeaderValues("header"));
        assertArrayEquals(new String[]{"application/json; charset=UTF-8"}, result.getHeaderValues("Content-Type"));
    }

    @Test
    public void test_failureRequest() throws Exception {
        try {
            resource.requestFailure();
            fail("failure endpoint should result in exception");
        } catch (FailureRESTException ex) {
            assertEquals(500, ex.getStatusCode());
            assertEquals("FAILURE_REQUEST_CALLED", ex.getResponseMessage());
            assertArrayEquals(new String[]{"headerValue"}, ex.getHeaderValues("header"));
            assertArrayEquals(new String[]{"application/json; charset=UTF-8"}, ex.getHeaderValues("Content-Type"));
            assertEquals("some reason", ex.failureReason);
        }
    }

    private SSLSocketFactory createSocketFactory() throws Exception {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[]{mockedTrustManager}, new SecureRandom());
        return context.getSocketFactory();
    }

    private void mockJSONParsers() throws Exception {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        when(mockedRequestParser.parse(any(), any(), any())).thenAnswer(invocationOnMock -> {
            Object body = invocationOnMock.getArgument(0);
            String charset = invocationOnMock.getArgument(2);
            try (ByteArrayOutputStream output = new ByteArrayOutputStream();
                 OutputStreamWriter outputWriter = new OutputStreamWriter(output, charset);
                 JsonWriter jsonWriter = new JsonWriter(outputWriter)) {
                gson.toJson(body, body.getClass(), jsonWriter);
                jsonWriter.flush();
                return (lastRequestParseResult = output.toByteArray());
            }
        });
        when(mockedResponseParser.parse(any(), any(), any(), any(), any())).thenAnswer(invocationOnMock -> {
            Class<?> type = invocationOnMock.getArgument(0);
            Response response = invocationOnMock.getArgument(2);
            String charset = invocationOnMock.getArgument(4);
            try (InputStreamReader reader = new InputStreamReader(response.getBodyStream(), charset)) {
                return (lastResponseParseResult = gson.fromJson(reader, type));
            }
        });
    }

    private void mockServer() {
        ConfigurationProperties.sslCertificateDomainName("unknown.com");
        Set<String> alternativeNameDomains = new HashSet<>();
        alternativeNameDomains.add("unk.com");
        ConfigurationProperties.sslSubjectAlternativeNameDomains(alternativeNameDomains);
        successRequest = HttpRequest.request()
                .withMethod("POST")
                .withPath("/resource/endpoint/success")
                .withBody(new org.mockserver.model.Body<byte[]>(org.mockserver.model.Body.Type.BINARY) {
                    @Override
                    public byte[] getValue() {
                        return lastRequestParseResult;
                    }
                }).withHeader("header", "headerValue")
                .withQueryStringParameter("queryParam", "queryParamValue");
        mockedServer.when(successRequest)
                .respond(HttpResponse.response()
                        .withStatusCode(200)
                        .withReasonPhrase("OK")
                        .withHeader("header", "headerValue")
                        .withHeader("Content-Type", "application/json; charset=UTF-8")
                        .withBody("{\"name\":\"some name\",\"age\":5,\"size\":3.5}", StandardCharsets.UTF_8));

        mockedServer.when(HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/resource/endpoint/failure"))
                .respond(HttpResponse.response()
                        .withStatusCode(500)
                        .withReasonPhrase("FAILURE_REQUEST_CALLED")
                        .withHeader("header", "headerValue")
                        .withHeader("Content-Type", "application/json; charset=UTF-8")
                        .withBody("{\"failureReason\":\"some reason\"}", StandardCharsets.UTF_8));

    }

    @Resource("/resource")
    private interface TestResource {
        @Endpoint(value = RequestMethod.POST, path = "/endpoint/{pathParam}")
        ResponseObject requestSuccess(@Header("header") String header, @PathParam("pathParam") String pathParam, @QueryParam("queryParam") String queryParam, @Body RequestObject body) throws Exception;

        @Error(startCode = 500, endCode = 500, value = FailureRESTException.class)
        @Endpoint(value = RequestMethod.GET, path = "/endpoint/failure")
        void requestFailure() throws Exception;
    }

    public static class FailureRESTException extends RESTException {
        @Expose
        private String failureReason;
    }

    public static class RequestObject {
        @Expose
        private final int maxAge;
        @Expose
        private final float maxSize;

        private RequestObject(int maxAge, float maxSize) {
            this.maxAge = maxAge;
            this.maxSize = maxSize;
        }
    }

    public static class ResponseObject extends RESTResponse {

        @Expose
        private String name;
        @Expose
        private int age;
        @Expose
        private float size;


    }

}
