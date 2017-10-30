@file:Suppress("PackageName")

package com.star_zero.sse

import com.nhaarman.mockito_kotlin.mock
import okio.BufferedSource
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertNull
import org.junit.Assert.assertThat
import org.junit.Test

class EventSourceReaderTest {

    @Test
    fun parseEvent() {
        val source = mock<BufferedSource>()
        val eventSource = EventSource("", Handler())
        val reader = eventSource.EventSourceReader(source)

        reader.parse("event: add")

        assertThat(reader.event, `is`("add"))
    }

    @Test
    fun parseData() {
        val source = mock<BufferedSource>()
        val eventSource = EventSource("", Handler())
        val reader = eventSource.EventSourceReader(source)

        reader.parse("data: value1")
        reader.parse("data: value2")

        assertThat(reader.data, `is`(listOf("value1", "value2")))
    }

    @Test
    fun parseId() {
        val source = mock<BufferedSource>()
        val eventSource = EventSource("", Handler())
        val reader = eventSource.EventSourceReader(source)

        reader.parse("id: 123")

        assertThat(reader.id, `is`("123"))
    }

    @Test
    fun parseRetry() {
        val source = mock<BufferedSource>()
        val eventSource = EventSource("", Handler())
        val reader = eventSource.EventSourceReader(source)

        reader.parse("retry: 1000")

        assertThat(eventSource.reconnectionTime, `is`(1000L))
    }

    @Test
    fun parseOther() {
        val source = mock<BufferedSource>()
        val eventSource = EventSource("", Handler())
        val reader = eventSource.EventSourceReader(source)

        reader.parse("dummy: 123")

        assertNull(reader.event)
        assertThat(reader.data, `is`(listOf<String>()))
        assertNull(reader.id)
    }

    private class Handler : EventHandler {
        override fun onOpen() {
        }

        override fun onMessage(event: MessageEvent) {
        }

        override fun onError(e: Exception) {
        }
    }
}