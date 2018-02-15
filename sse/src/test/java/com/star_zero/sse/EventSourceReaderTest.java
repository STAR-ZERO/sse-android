package com.star_zero.sse;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.junit.Test;

import okio.BufferedSource;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class EventSourceReaderTest {

    @Test
    public void parseEvent() {
        BufferedSource source = mock(BufferedSource.class);
        EventSource eventSource = new EventSource("", new Handler());
        EventSource.EventSourceReader reader = eventSource.new EventSourceReader(source);

        reader.parse("event: add");
        assertThat(reader.event, is("add"));
    }

    @Test
    public void parseData() {
        BufferedSource source = mock(BufferedSource.class);
        EventSource eventSource = new EventSource("", new Handler());
        EventSource.EventSourceReader reader = eventSource.new EventSourceReader(source);

        reader.parse("data: value1");
        reader.parse("data: value2");

        assertThat(reader.data, contains("value1", "value2"));
    }

    @Test
    public void parseId() {
        BufferedSource source = mock(BufferedSource.class);
        EventSource eventSource = new EventSource("", new Handler());
        EventSource.EventSourceReader reader = eventSource.new EventSourceReader(source);

        reader.parse("id: 123");

        assertThat(reader.id, is("123"));
    }

    @Test
    public void parseRetry() {
        BufferedSource source = mock(BufferedSource.class);
        EventSource eventSource = new EventSource("", new Handler());
        EventSource.EventSourceReader reader = eventSource.new EventSourceReader(source);

        reader.parse("retry: 1000");

        assertThat(eventSource.getReconnectionTime(), is(1000L));
    }

    @Test
    public void parseOther() {
        BufferedSource source = mock(BufferedSource.class);
        EventSource eventSource = new EventSource("", new Handler());
        EventSource.EventSourceReader reader = eventSource.new EventSourceReader(source);

        reader.parse("dummy: 123");

        assertNull(reader.event);
        assertTrue(reader.data.isEmpty());
        assertNull(reader.id);
    }

    private static class Handler implements EventHandler {
        @Override
        public void onOpen() {
        }

        @Override
        public void onMessage(@NonNull MessageEvent event) {
        }

        @Override
        public void onError(@Nullable Exception e) {
        }
    }
}
