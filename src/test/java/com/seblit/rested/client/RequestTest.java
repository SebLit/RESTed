package com.seblit.rested.client;

import org.junit.Test;

import static org.junit.Assert.*;

public class RequestTest {

    @Test
    public void testBuilder_build() {
        String path = "path";
        RequestMethod method = RequestMethod.GET;
        byte[] body = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        String header = "header";
        String headerValue = "hValue";
        String param = "param";
        String paramValue = "pValue";

        Request request = new Request.Builder()
                .setMethod(method)
                .setPath(path)
                .setBody(body)
                .addHeader(header, headerValue)
                .addQueryParam(param, paramValue)
                .build();

        assertEquals(method, request.getMethod());
        assertEquals(path, request.getPath());
        assertArrayEquals(body, request.getBody());
        assertArrayEquals(new String[]{headerValue}, request.getHeaderValues(header));
        assertTrue(request.hasQueryParam(param));
        assertFalse(request.hasQueryParam(param+"_other"));
        assertArrayEquals(new String[]{paramValue}, request.getQueryParamValues(param));
    }

    @Test
    public void testBuilder_method() {
        Request.Builder builder = new Request.Builder();
        assertSame(builder, builder.setMethod(RequestMethod.GET));
        assertEquals(RequestMethod.GET, builder.getMethod());
    }

    @Test
    public void testBuilder_path() {
        String testPath = "testPath";
        Request.Builder builder = new Request.Builder();
        assertSame(builder, builder.setPath(testPath));
        assertEquals(testPath, builder.getPath());
    }

    @Test
    public void testBuilder_body() {
        byte[] testBody = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Request.Builder builder = new Request.Builder();
        assertSame(builder, builder.setBody(testBody));
        assertEquals(testBody, builder.getBody());
    }

    @Test
    public void testBuilder_addHeader() {
        String header = "header";
        String value1 = "value1";
        String value2 = "value2";
        Request.Builder builder = new Request.Builder();
        assertSame(builder, builder.addHeader(header, value1));
        assertSame(builder, builder.addHeader(header, value2));
        assertArrayEquals(new String[]{value1, value2}, builder.getHeaderValues(header));
    }

    @Test
    public void testBuilder_removeHeader() {
        String header = "header";
        Request.Builder builder = new Request.Builder().addHeader(header, "value1").addHeader(header, "value2");
        assertSame(builder, builder.removeHeader(header));
        assertNull(builder.getHeaderValues(header));
    }

    @Test
    public void testBuilder_removeHeaderValue() {
        String header = "header";
        String value1 = "value1";
        String value2 = "value2";
        Request.Builder builder = new Request.Builder().addHeader(header, value1).addHeader(header, value2);
        assertSame(builder, builder.removeHeader(header, value1));
        assertArrayEquals(new String[]{value2}, builder.getHeaderValues(header));
    }

    @Test
    public void testBuilder_addQueryParam() {
        String param = "param";
        String value1 = "value1";
        String value2 = "value2";
        Request.Builder builder = new Request.Builder();
        assertSame(builder, builder.addQueryParam(param, value1));
        assertSame(builder, builder.addQueryParam(param, value2));
        assertArrayEquals(new String[]{value1, value2}, builder.getQueryParamValues(param));
    }

    @Test
    public void testBuilder_removeQueryParam() {
        String param = "param";
        Request.Builder builder = new Request.Builder().addQueryParam(param, "value1").addQueryParam(param, "value2");
        assertSame(builder, builder.removeQueryParam(param));
        assertNull(builder.getQueryParamValues(param));
    }

    @Test
    public void testBuilder_removeQueryParamValue() {
        String param = "param";
        String value1 = "value1";
        String value2 = "value2";
        Request.Builder builder = new Request.Builder().addQueryParam(param, value1).addQueryParam(param, value2);
        assertSame(builder, builder.removeQueryParam(param, value1));
        assertArrayEquals(new String[]{value2}, builder.getQueryParamValues(param));
    }

    @Test
    public void testBuilder_hasQueryParam() {
        String param = "param";
        Request.Builder builder = new Request.Builder().addQueryParam(param, "value");
        assertTrue(builder.hasQueryParam(param));
        assertFalse(builder.hasQueryParam(param + "_other"));
    }

    @Test
    public void testBuilder_getQueryParam() {
        String param1 = "param1";
        String param2 = "param2";
        Request.Builder builder = new Request.Builder().addQueryParam(param1, "value1").addQueryParam(param2, "value2");
        assertArrayEquals(new String[]{param1, param2}, builder.getQueryParams());
    }

}
