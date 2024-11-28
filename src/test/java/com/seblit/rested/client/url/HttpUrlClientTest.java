package com.seblit.rested.client.url;

import com.seblit.rested.client.HTTPClient;
import com.seblit.rested.client.Response;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.lang.reflect.Method;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class HttpUrlClientTest extends MockServerSetup {

    HTTPClient client;

    @Before
    public void setup() throws Exception{
        client = new HttpUrlClient(TEST_HOST, TEST_PORT);
    }

    @Test
    public void testRequest_success() throws Exception {
        Response response = client.request(mockedSuccessRequest, mock(Method.class), null);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testRequest_error() throws Exception {
        Response response = client.request(mockedErrorRequest, mock(Method.class), null);
        assertEquals(500, response.getStatusCode());
    }

    @Test
    public void testRequest_resBody() throws Exception {
        try (Response response = client.request(mockedResBodyRequest, mock(Method.class), null); InputStream stream = response.getBodyStream()) {
            byte[] data = new byte[stream.available()];
            stream.read(data);
            assertArrayEquals(TEST_BODY, data);
        }
    }

    @Test
    public void testRequest_resHeaders() throws Exception{
        Response response = client.request(mockedResHeaderRequest, mock(Method.class), null);
        assertArrayEquals(TEST_HEADER_SINGLE_VALUE, response.getHeaderValues(TEST_HEADER_SINGLE));
        assertArrayEquals(TEST_HEADER_MULTIPLE_VALUE, response.getHeaderValues(TEST_HEADER_MULTIPLE));
    }

    @Test
    public void testRequest_reqBody() throws Exception{
        client.request(mockedReqBodyRequest, mock(Method.class), null);
        verifyRequest(mockedReqBodyRequest);
    }

    @Test
    public void testRequest_reqHeaders() throws Exception{
        client.request(mockedReqHeaderRequest, mock(Method.class), null);
        verifyRequest(mockedReqHeaderRequest);
    }

    @Test
    public void testRequest_queryParams() throws Exception{
        client.request(mockedQueryParamRequest, mock(Method.class), null);
        verifyRequest(mockedQueryParamRequest);
    }

}
