package com.seblit.rested.client;

import static org.junit.Assert.*;
import org.junit.Test;

public class RESTResponseTest {

    @Test
    public void testInit(){
        int statusCode = 1;
        String message = "message";
        RESTResponse response = new RESTResponse();
        response.init(statusCode, message, null);

        assertEquals(statusCode, response.getStatusCode());
        assertEquals(message, response.getResponseMessage());
    }

}
