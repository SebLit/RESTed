package com.seblit.rested.client;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class StreamedRestResponseTest {

    @Test
    public void testInit(){
        InputStream mockedBodyStream = mock(InputStream.class);
        StreamedRESTResponse response = new StreamedRESTResponse(mockedBodyStream);
        assertSame(mockedBodyStream, response.getBodyStream());
    }

    @Test
    public void testClose() throws Exception {
        InputStream mockedBodyStream = mock(InputStream.class);
        StreamedRESTResponse response = new StreamedRESTResponse(mockedBodyStream);
        response.close();
        verify(mockedBodyStream).close();
    }

}
