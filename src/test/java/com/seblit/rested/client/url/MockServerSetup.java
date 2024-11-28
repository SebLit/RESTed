package com.seblit.rested.client.url;

import com.seblit.rested.client.Request;
import com.seblit.rested.client.RequestMethod;
import org.junit.After;
import org.junit.Before;

import static org.mockito.Mockito.*;

import org.junit.BeforeClass;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class MockServerSetup {

    static final int TEST_PORT = 1234;
    static final String TEST_HOST = "localhost";
    static final byte[] TEST_BODY = "responseBody".getBytes(StandardCharsets.UTF_8);
    static final String TEST_HEADER_SINGLE = "singleValueHeader";
    static final String TEST_HEADER_MULTIPLE = "multiValueHeader";
    static final String[] TEST_HEADER_SINGLE_VALUE = {"value"};
    static final String[] TEST_HEADER_MULTIPLE_VALUE = {"value1", "value2"};
    static final String TEST_QUERY_PARAM_SINGLE = "singleValueQueryParam";
    static final String TEST_QUERY_PARAM_MULTIPLE = "multiValueQueryParam";
    static final String[] TEST_QUERY_PARAM_SINGLE_VALUE = {"value"};
    static final String[] TEST_QUERY_PARAM_MULTIPLE_VALUE = {"value1", "value2"};

    static final Request mockedSuccessRequest = mock(Request.class);
    static final Request mockedErrorRequest = mock(Request.class);
    static final Request mockedResBodyRequest = mock(Request.class);
    static final Request mockedResHeaderRequest = mock(Request.class);
    static final Request mockedReqBodyRequest = mock(Request.class);
    static final Request mockedReqHeaderRequest = mock(Request.class);
    static final Request mockedQueryParamRequest = mock(Request.class);

    private ClientAndServer mockedServer;

    @BeforeClass
    public static void setupRequests() {
        configureRequest(mockedSuccessRequest, RequestMethod.GET, "/test/status/200", null);
        configureRequest(mockedErrorRequest, RequestMethod.GET, "/test/status/500", null);
        configureRequest(mockedResBodyRequest, RequestMethod.GET, "/test/response/body", null);
        configureRequest(mockedResHeaderRequest, RequestMethod.GET, "/test/response/headers", null);
        configureRequest(mockedReqBodyRequest, RequestMethod.POST, "/test/request/body", TEST_BODY);
        configureRequest(mockedReqHeaderRequest, RequestMethod.GET, "/test/request/headers", null);
        when(mockedReqHeaderRequest.getHeaders()).thenReturn(new String[]{TEST_HEADER_SINGLE, TEST_HEADER_MULTIPLE});
        when(mockedReqHeaderRequest.getHeaderValues(TEST_HEADER_SINGLE)).thenReturn(TEST_HEADER_SINGLE_VALUE);
        when(mockedReqHeaderRequest.getHeaderValues(TEST_HEADER_MULTIPLE)).thenReturn(TEST_HEADER_MULTIPLE_VALUE);
        configureRequest(mockedQueryParamRequest, RequestMethod.GET, "/test/request/params", null);
        when(mockedQueryParamRequest.getQueryParams()).thenReturn(new String[]{TEST_QUERY_PARAM_SINGLE, TEST_QUERY_PARAM_MULTIPLE});
        when(mockedQueryParamRequest.getQueryParamValues(TEST_QUERY_PARAM_SINGLE)).thenReturn(TEST_QUERY_PARAM_SINGLE_VALUE);
        when(mockedQueryParamRequest.getQueryParamValues(TEST_QUERY_PARAM_MULTIPLE)).thenReturn(TEST_QUERY_PARAM_MULTIPLE_VALUE);
    }

    @Before
    public void setupServer() {
        ConfigurationProperties.sslCertificateDomainName("unknown.com");
        Set<String> alternativeNameDomains = new HashSet<>();
        alternativeNameDomains.add("unk.com");
        ConfigurationProperties.sslSubjectAlternativeNameDomains(alternativeNameDomains);
        mockedServer = ClientAndServer.startClientAndServer(TEST_PORT);
        mockedServer.when(createMockServerRequest(mockedSuccessRequest))
                .respond(HttpResponse.response().withStatusCode(200));
        mockedServer.when(createMockServerRequest(mockedErrorRequest))
                .respond(HttpResponse.response().withStatusCode(500));
        mockedServer.when(createMockServerRequest(mockedResBodyRequest))
                .respond(HttpResponse.response().withStatusCode(200).withBody(TEST_BODY));
        mockedServer.when(createMockServerRequest(mockedResHeaderRequest))
                .respond(HttpResponse.response().withStatusCode(200)
                        .withHeader(TEST_HEADER_SINGLE, TEST_HEADER_SINGLE_VALUE)
                        .withHeader(TEST_HEADER_MULTIPLE, TEST_HEADER_MULTIPLE_VALUE));
    }

    @After
    public void stopServer() {
        mockedServer.stop();
    }

    void verifyRequest(Request request) {
        mockedServer.verify(createMockServerRequest(request));
    }

    private HttpRequest createMockServerRequest(Request request) {
        HttpRequest mockServerRequest = HttpRequest.request()
                .withMethod(request.getMethod().name())
                .withPath(request.getPath())
                .withBody(request.getBody());
        for (String header : request.getHeaders()) {
            mockServerRequest.withHeader(header, request.getHeaderValues(header));
        }
        for (String queryParam : request.getQueryParams()) {
            mockServerRequest.withQueryStringParameter(queryParam, request.getQueryParamValues(queryParam));
        }
        return mockServerRequest;
    }

    private static void configureRequest(Request mockedRequest, RequestMethod method, String path, byte[] body) {
        when(mockedRequest.getMethod()).thenReturn(method);
        when(mockedRequest.getPath()).thenReturn(path);
        when(mockedRequest.getHeaders()).thenReturn(new String[0]);
        when(mockedRequest.getQueryParams()).thenReturn(new String[0]);
        when(mockedRequest.getBody()).thenReturn(body);
    }

}
