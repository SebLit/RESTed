package com.seblit.rested.client;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class HeaderHolderTest {

    @Test
    public void test_noHeaders() {
        HeaderHolder holder = new HeaderHolder(null);
        assertEquals(0, holder.getHeaders().length);
    }

    @Test
    public void test_withHeaders() {
        Map<String, List<String>> testHeaders =  new HashMap<>();
        testHeaders.computeIfAbsent("header1", s -> new ArrayList<>()).add("value11");
        testHeaders.get("header1").add("value12");
        testHeaders.computeIfAbsent("header2", s -> new ArrayList<>()).add("value21");
        testHeaders.get("header2").add("value22");

        HeaderHolder holder = new HeaderHolder(testHeaders);
        testHeaders.forEach((header, values) -> assertArrayEquals(values.toArray(new String[0]), holder.getHeaderValues(header)));
        assertEquals(testHeaders.keySet().size(), holder.getHeaders().length);
    }

    @Test
    public void testSetHeaders() {
        HeaderHolder holder = new HeaderHolder(null);
        Map<String, List<String>> testHeaders =  new HashMap<>();
        testHeaders.computeIfAbsent("header1", s -> new ArrayList<>()).add("value11");
        testHeaders.get("header1").add("value12");
        testHeaders.computeIfAbsent("header2", s -> new ArrayList<>()).add("value21");
        testHeaders.get("header2").add("value22");
        holder.setHeaders(testHeaders);

        testHeaders.forEach((header, values) -> assertArrayEquals(values.toArray(new String[0]), holder.getHeaderValues(header)));
        assertEquals(testHeaders.keySet().size(), holder.getHeaders().length);
    }
    
    @Test
    public void testHasHeader(){
        Map<String, List<String>> testHeaders =  new HashMap<>();
        testHeaders.computeIfAbsent("header1", s -> new ArrayList<>()).add("value11");
        testHeaders.get("header1").add("value12");
        testHeaders.computeIfAbsent("header2", s -> new ArrayList<>()).add("value21");
        testHeaders.get("header2").add("value22");
        HeaderHolder holder = new HeaderHolder(testHeaders);

        testHeaders.forEach((header, values) -> assertTrue(holder.hasHeader(header)));
        assertFalse(holder.hasHeader("header3"));
    }

}
