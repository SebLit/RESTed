package com.seblit.rested.client;

import org.junit.Test;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseTest {

    @Test
    public void testInit(){
        int statusCode = 1;
        String message = "message";
        String header = "header";
        String headerValue = "value";
        InputStream headers = mock(InputStream.class);
        Map<String, List<String>> mockedHeaders = new HashMap<>();
        mockedHeaders.computeIfAbsent(header, s -> new ArrayList<>()).add(headerValue);

        Response response = new Response(statusCode, message, headers, mockedHeaders);
        assertEquals(statusCode, response.getStatusCode());
        assertEquals(message, response.getMessage());
        assertEquals(headers, response.getBodyStream());
        assertArrayEquals(new String[]{headerValue}, response.getHeaderValues(header));
    }

    @Test
    public void testisSuccessResponse_true(){
        for(int i = 200; i < 300; i++){
            assertTrue(new Response(i, null, null, null).isSuccessResponse());
        }
    }

    @Test
    public void testisSuccessResponse_false(){
        for(int i = 100;i<600; i+=100){
            if(i == 200){
                continue;
            }
            assertFalse(new Response(i, null, null, null).isSuccessResponse());
        }
    }

    @Test
    public void testClose() throws Exception {
        InputStream mockedStream = mock(InputStream.class);
        new Response(0, null, mockedStream, null).close();
        verify(mockedStream).close();
    }
    
}
