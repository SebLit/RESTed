package com.seblit.rested.client;

import com.seblit.rested.client.annotation.*;
import com.seblit.rested.client.annotation.Error;
import com.seblit.rested.client.media.MissingRequestParserException;
import com.seblit.rested.client.media.MissingResponseParserException;
import com.seblit.rested.client.media.RequestBodyParser;
import com.seblit.rested.client.media.ResponseBodyParser;

import static org.junit.Assert.*;

import com.seblit.rested.client.middleware.RequestInterceptedException;
import com.seblit.rested.client.middleware.RequestInterceptor;
import com.seblit.rested.client.middleware.ResponseInterceptedException;
import com.seblit.rested.client.middleware.ResponseInterceptor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.InputStream;
import java.util.*;

import static org.mockito.Mockito.*;

public class ResourceFactoryTest {

    private static final String DEFAULT_RESULT = "defaultResult";
    private static final byte[] DEFAULT_REQUEST_BODY = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    private ResourceFactory factory;
    private HTTPClient mockedClient;
    private RequestBodyParser mockedRequestParser;
    private ResponseBodyParser mockedResponseParser;
    private RequestInterceptor mockedRequestInterceptor;
    private ResponseInterceptor mockedResponseInterceptor;
    private Response response;
    private ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
    private ArgumentCaptor<Request.Builder> reqInterceptorRequestCaptor = ArgumentCaptor.forClass(Request.Builder.class);
    private ArgumentCaptor<Object> reqInterceptorBodyCaptor = ArgumentCaptor.forClass(Object.class);

    private ArgumentCaptor<Request> resInterceptorRequestCaptor = ArgumentCaptor.forClass(Request.class);
    private ArgumentCaptor<Response> resInterceptorResponseCaptor = ArgumentCaptor.forClass(Response.class);
    private ArgumentCaptor<Object> resInterceptorBodyCaptor = ArgumentCaptor.forClass(Object.class);


    @Before
    public void setup() throws Exception {
        mockedClient = mock(HTTPClient.class);
        mockedRequestParser = mock(RequestBodyParser.class);
        mockedResponseParser = mock(ResponseBodyParser.class);
        mockedRequestInterceptor = mock(RequestInterceptor.class);
        mockedResponseInterceptor = mock(ResponseInterceptor.class);
        response = new Response(200, "message", mock(InputStream.class), null);
        response.headers.put("Content-Type", Collections.singletonList("application/xml; charset=UTF-16"));

        when(mockedClient.request(requestCaptor.capture(), any(), any())).thenReturn(response);
        when(mockedRequestParser.parse(any(), any(), any())).thenReturn(DEFAULT_REQUEST_BODY);
        when(mockedResponseParser.parse(any(), any(), any(), anyString(), anyString())).thenReturn(DEFAULT_RESULT);
        doNothing().when(mockedRequestInterceptor).intercept(reqInterceptorRequestCaptor.capture(), reqInterceptorBodyCaptor.capture(), any(), any());
        doNothing().when(mockedResponseInterceptor).intercept(resInterceptorRequestCaptor.capture(), resInterceptorResponseCaptor.capture(), resInterceptorBodyCaptor.capture(), any(), any());

        factory = new ResourceFactory(mockedClient);
        factory.registerRequestParser(mockedRequestParser, "*/*");
        factory.registerResponseParser(mockedResponseParser, "*/*");
        factory.addRequestInterceptors(mockedRequestInterceptor);
        factory.addResponseInterceptors(mockedResponseInterceptor);
    }

    /* Middleware tests */
    @Test(expected = RequestInterceptedException.class)
    public void testInterceptRequest_intercepted() throws Exception {
        doThrow(RequestInterceptedException.class).when(mockedRequestInterceptor).intercept(any(), any(), any(), any());
        factory.createResource(TestResource.class).request(null, "", null, null);
    }

    @Test
    public void testInterceptRequest_allowed() throws Exception {
        String headerValue = "headerValue";
        String pathParam = "pathValue";
        String queryParam = "queryValue";
        Object body = new Object();
        factory.createResource(TestResource.class).request(headerValue, pathParam, queryParam, body);
        Request.Builder interceptedRequest = reqInterceptorRequestCaptor.getValue();

        assertArrayEquals(new String[]{headerValue}, interceptedRequest.getHeaderValues("header"));
        assertEquals("/test/request/" + pathParam, interceptedRequest.getPath());
        assertArrayEquals(new String[]{queryParam}, interceptedRequest.getQueryParamValues("queryParam"));
        assertEquals(RequestMethod.POST, interceptedRequest.getMethod());
        assertSame(body, reqInterceptorBodyCaptor.getValue());
    }

    @Test
    public void testInterceptRequest_unregister() throws Exception {
        factory.removeRequestInterceptors(mockedRequestInterceptor);
        factory.createResource(TestResource.class).request(null, "", null, null);
        verify(mockedRequestInterceptor, never()).intercept(any(), any(), any(), any());
    }

    @Test(expected = ResponseInterceptedException.class)
    public void testInterceptResponse_intercepted() throws Exception {
        doThrow(ResponseInterceptedException.class).when(mockedResponseInterceptor).intercept(any(), any(), any(), any(), any());
        factory.createResource(TestResource.class).request(null, "", null, null);
    }

    @Test
    public void testInterceptResponse_allowed() throws Exception {
        String headerValue = "headerValue";
        String pathParam = "pathValue";
        String queryParam = "queryValue";
        factory.createResource(TestResource.class).request(headerValue, pathParam, queryParam, null);
        Request interceptedRequest = resInterceptorRequestCaptor.getValue();
        Response interceptedResponse = resInterceptorResponseCaptor.getValue();

        assertArrayEquals(new String[]{headerValue}, interceptedRequest.getHeaderValues("header"));
        assertEquals("/test/request/" + pathParam, interceptedRequest.getPath());
        assertArrayEquals(new String[]{queryParam}, interceptedRequest.getQueryParamValues("queryParam"));
        assertEquals(RequestMethod.POST, interceptedRequest.getMethod());
        assertSame(DEFAULT_RESULT, resInterceptorBodyCaptor.getValue());
        assertSame(response, interceptedResponse);

    }

    @Test
    public void testInterceptResponse_unregister() throws Exception {
        factory.removeResponseInterceptors(mockedResponseInterceptor);
        factory.createResource(TestResource.class).request(null, "", null, null);
        verify(mockedResponseInterceptor, never()).intercept(any(), any(), any(), any(), any());
    }

    /* Parser tests */
    @Test
    public void testRequestParser_explicit() throws Exception {
        RequestBodyParser parser = mock(RequestBodyParser.class);
        when(parser.parse(any(), any(), any())).thenReturn(DEFAULT_REQUEST_BODY);
        Object body = new Object();
        factory.registerRequestParser(parser, "application/xml");
        factory.createResource(TestResource.class).request(null, null, "", body);
        verify(parser).parse(body, "application/xml", "UTF-16");
        verify(mockedRequestParser, never()).parse(any(), any(), any());
    }

    @Test
    public void testRequestParser_subtype() throws Exception {
        RequestBodyParser parser = mock(RequestBodyParser.class);
        when(parser.parse(any(), any(), any())).thenReturn(DEFAULT_REQUEST_BODY);
        Object body = new Object();
        factory.registerRequestParser(parser, "application/*");
        factory.createResource(TestResource.class).request(null, null, "", body);
        verify(parser).parse(body, "application/xml", "UTF-16");
        verify(mockedRequestParser, never()).parse(any(), any(), any());
    }

    @Test
    public void testRequestParser_generic() throws Exception {
        Object body = new Object();
        factory.createResource(TestResource.class).request(null, null, "", body);
        verify(mockedRequestParser).parse(body, "application/xml", "UTF-16");
    }

    @Test(expected = MissingRequestParserException.class)
    public void testRequestParser_unregister() throws Exception {
        factory.unregisterRequestParser("*/*");
        factory.createResource(TestResource.class).request(null, null, "", new Object());
    }

    @Test
    public void testResponseParser_explicit() throws Exception {
        ResponseBodyParser parser = mock(ResponseBodyParser.class);
        when(parser.parse(any(), any(), any(), any(), any())).thenReturn(DEFAULT_RESULT);
        Object body = new Object();
        factory.registerResponseParser(parser, "application/xml");
        factory.createResource(TestResource.class).request(null, null, "", body);
        verify(parser).parse(same(String.class), any(), same(response), eq("application/xml"), eq("UTF-16"));
        verify(mockedResponseParser, never()).parse(any(), any(), any(), any(), any());
    }

    @Test
    public void testResponseParser_subtype() throws Exception {
        ResponseBodyParser parser = mock(ResponseBodyParser.class);
        when(parser.parse(any(), any(), any(), any(), any())).thenReturn(DEFAULT_RESULT);
        Object body = new Object();
        factory.registerResponseParser(parser, "application/*");
        factory.createResource(TestResource.class).request(null, null, "", body);
        verify(parser).parse(same(String.class), any(), same(response), eq("application/xml"), eq("UTF-16"));
        verify(mockedResponseParser, never()).parse(any(), any(), any(), any(), any());
    }

    @Test
    public void testResponseParser_generic() throws Exception {
        Object body = new Object();
        factory.createResource(TestResource.class).request(null, null, "", body);
        verify(mockedResponseParser).parse(same(String.class), any(), same(response), eq("application/xml"), eq("UTF-16"));
    }

    @Test(expected = MissingResponseParserException.class)
    public void testResponseParser_unregister() throws Exception {
        factory.unregisterResponseParser("*/*");
        factory.createResource(TestResource.class).request(null, null, "", new Object());
    }

    /* Resource tests */
    @Test
    public void testRequest_headers() throws Exception {
        String headerValue = "value";
        factory.createResource(TestResource.class).request(headerValue, "", null, null);
        Request request = requestCaptor.getValue();

        assertArrayEquals(new String[]{headerValue}, request.getHeaderValues("header"));
    }

    @Test
    public void testRequest_headersIterable() throws Exception {
        String headerValue1 = "value1";
        String headerValue2 = "value2";
        factory.createResource(TestResource.class).request_headerIterable(Arrays.asList(headerValue1, headerValue2));
        Request request = requestCaptor.getValue();

        assertArrayEquals(new String[]{headerValue1, headerValue2}, request.getHeaderValues("header"));
    }

    @Test
    public void testRequest_headersArray() throws Exception {
        String headerValue1 = "value1";
        String headerValue2 = "value2";
        factory.createResource(TestResource.class).request_headerArray(headerValue1, headerValue2);
        Request request = requestCaptor.getValue();

        assertArrayEquals(new String[]{headerValue1, headerValue2}, request.getHeaderValues("header"));

    }

    @Test
    public void testRequest_path() throws Exception {
        String pathParam = "pathParam";
        factory.createResource(TestResource.class).request(null, pathParam, null, null);
        Request request = requestCaptor.getValue();

        assertEquals("/test/request/" + pathParam, request.getPath());
    }

    @Test
    public void testRequest_queryParam() throws Exception {
        String queryValue = "value";
        factory.createResource(TestResource.class).request(null, "", queryValue, null);
        Request request = requestCaptor.getValue();

        assertArrayEquals(new String[]{queryValue}, request.getQueryParamValues("queryParam"));
    }

    @Test
    public void testRequest_queryParamIterable() throws Exception {
        String queryValue1 = "value1";
        String queryValue2 = "value2";
        factory.createResource(TestResource.class).request_queryIterable(Arrays.asList(queryValue1, queryValue2));
        Request request = requestCaptor.getValue();

        assertArrayEquals(new String[]{queryValue1, queryValue2}, request.getQueryParamValues("queryParam"));
    }

    @Test
    public void testRequest_queryParamArray() throws Exception {
        String queryValue1 = "value1";
        String queryValue2 = "value2";
        factory.createResource(TestResource.class).request_queryArray(queryValue1, queryValue2);
        Request request = requestCaptor.getValue();

        assertArrayEquals(new String[]{queryValue1, queryValue2}, request.getQueryParamValues("queryParam"));

    }

    @Test
    public void testRequest_body() throws Exception {
        factory.createResource(TestResource.class).request(null, "", null, new Object());
        Request request = requestCaptor.getValue();

        assertArrayEquals(DEFAULT_REQUEST_BODY, request.getBody());
    }

    @Test
    public void testRequest_returnType() throws Exception {
        String result = factory.createResource(TestResource.class).request(null, "", null, null);
        assertSame(DEFAULT_RESULT, result);
    }

    @Test
    public void testRequest_returnTypeREST() throws Exception {
        String message = "message";
        Map<String, List<String>> responseHeaders = new HashMap<>();
        responseHeaders.put("header1", Arrays.asList("value1", "value2"));
        responseHeaders.put("header2", Arrays.asList("value3"));
        response.setHeaders(responseHeaders);

        when(mockedResponseParser.parse(any(), any(), any(), any(), any())).thenReturn(new RESTResponse());
        RESTResponse result = factory.createResource(TestResource.class).request_returnTypeREST();

        assertEquals(200, result.getStatusCode());
        assertEquals(message, result.getResponseMessage());
        responseHeaders.forEach((header, values) -> assertArrayEquals(values.toArray(new String[0]), result.getHeaderValues(header)));
    }

    @Test
    public void testRequest_returnTypeStreamedREST() throws Exception {
        StreamedRESTResponse result = factory.createResource(TestResource.class).request_returnTypeStreamedREST();
        assertSame(response.getBodyStream(), result.getBodyStream());
        verify(mockedResponseParser, never()).parse(any(), any(), any(), any(), any());
    }

    @Test
    public void testRequest_ReturnTypeNone() throws Exception {
        factory.createResource(TestResource.class).request_returnTypeNone();
        verify(mockedResponseParser, never()).parse(any(), any(), any(), any(), any());
    }

    @Test
    public void testRequest_errorREST() throws Exception {
        int statusCode = 500;
        String message = "message";
        Map<String, List<String>> responseHeaders = new HashMap<>();
        responseHeaders.put("header", Arrays.asList("value"));
        Response response = new Response(statusCode, message, mock(InputStream.class), responseHeaders);
        when(mockedClient.request(any(), any(), any())).thenReturn(response);
        when(mockedResponseParser.parse(same(RESTException.class), any(), any(), any(), any())).thenReturn(new RESTException());
        try {
            factory.createResource(TestResource.class).request(null, "", null, null);
            Assert.fail("Expected RESTException");
        } catch (RESTException e) {
            assertEquals(statusCode, e.getStatusCode());
            assertEquals(message, e.getResponseMessage());
            responseHeaders.forEach((header, values) -> assertArrayEquals(values.toArray(new String[0]), e.getHeaderValues(header)));
            assertNotNull(e.getRequest());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testRequest_errorCustom() throws Exception {
        Response response = new Response(100, null, mock(InputStream.class), null);
        when(mockedClient.request(any(), any(), any())).thenReturn(response);
        when(mockedResponseParser.parse(same(IllegalStateException.class), any(), any(), any(), any())).thenReturn(new IllegalStateException());
        factory.createResource(TestResource.class).request(null, "", null, null);
    }

    @Test
    public void testRequest_errorStreamedREST() throws Exception {
        Response response = new Response(300, null, mock(InputStream.class), null);
        when(mockedClient.request(any(), any(), any())).thenReturn(response);
        try {
            factory.createResource(TestResource.class).request(null, "", null, null);
        } catch (StreamedRESTException e) {
            assertSame(response.getBodyStream(), e.getBodyStream());
        }
    }

    @Resource("/test")
    private interface TestResource {
        @Endpoint(value = RequestMethod.POST, path = "/request/{pathParam}", charsets = "UTF-16", mediaTypes = "application/xml")
        @Error(startCode = 100, endCode = 199, value = IllegalStateException.class)
        @Error(startCode = 300, endCode = 399, value = StreamedRESTException.class)
        String request(@Header("header") String header, @PathParam("pathParam") String pathParam, @QueryParam("queryParam") String queryParam, @Body(value = "application/xml",charset = "UTF-16") Object body) throws Exception;

        @Endpoint(RequestMethod.GET)
        void request_headerIterable(@Header("header") Iterable<String> headers) throws Exception;

        @Endpoint(RequestMethod.GET)
        void request_headerArray(@Header("header") String... headers) throws Exception;

        @Endpoint(RequestMethod.GET)
        void request_queryIterable(@QueryParam("queryParam") Iterable<String> params) throws Exception;

        @Endpoint(RequestMethod.GET)
        void request_queryArray(@QueryParam("queryParam") String... params) throws Exception;

        @Endpoint(RequestMethod.GET)
        void request_returnTypeNone() throws Exception;

        @Endpoint(RequestMethod.GET)
        RESTResponse request_returnTypeREST() throws Exception;

        @Endpoint(RequestMethod.GET)
        StreamedRESTResponse request_returnTypeStreamedREST() throws Exception;
    }

}
