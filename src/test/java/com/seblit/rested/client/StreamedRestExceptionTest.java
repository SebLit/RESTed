package com.seblit.rested.client;

import org.junit.Test;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

import java.io.InputStream;

public class StreamedRestExceptionTest {

    @Test
    public void testInit(){
        InputStream mockedBodyStream = mock(InputStream.class);
        StreamedRESTException ex = new StreamedRESTException();
        ex.init(mockedBodyStream);
        assertSame(mockedBodyStream, ex.getBodyStream());
    }

    @Test
    public void testClose() throws Exception {
        InputStream mockedBodyStream = mock(InputStream.class);
        StreamedRESTException ex = new StreamedRESTException();
        ex.init(mockedBodyStream);
        ex.close();
        verify(mockedBodyStream).close();
    }

}
