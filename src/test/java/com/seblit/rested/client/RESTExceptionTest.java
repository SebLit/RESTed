package com.seblit.rested.client;

import org.junit.Test;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RESTExceptionTest {

    @Test
    public void testInit() {
        int statusCode = 1;
        String message = "message";
        String header = "header";
        String headerValue = "value";
        Map<String, List<String>> testHeaders = new HashMap<>();
        testHeaders.computeIfAbsent(header, s -> new ArrayList<>()).add(headerValue);
        Request mockedRequest = mock(Request.class);

        RESTException ex = new RESTException();
        ex.init(statusCode, message, testHeaders, mockedRequest);

        assertEquals(statusCode, ex.getStatusCode());
        assertEquals(message, ex.getResponseMessage());
        assertEquals(mockedRequest, ex.getRequest());
        assertArrayEquals(new String[]{headerValue}, ex.getHeaderValues(header));
    }

    @Test
    public void testHasHeader() {
        Map<String, List<String>> testHeaders = new HashMap<>();
        String header = "header";
        testHeaders.computeIfAbsent(header, s -> new ArrayList<>()).add("value");

        RESTException ex = new RESTException();
        ex.init(1, null, testHeaders, mock(Request.class));

        assertTrue(ex.hasHeader(header));
        assertFalse(ex.hasHeader(header + "_other"));
    }

}
