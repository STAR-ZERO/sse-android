@file:Suppress("PackageName")

package com.star_zero.sse

import okhttp3.*
import okio.BufferedSource
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class EventSource(private val url: String, private val eventHandler: EventHandler) {

    companion object {
        val CONNECTING = 0
        val OPEN = 1
        val CLOSED = 2
    }

    var readTimeout = 0L

    var readyState = CLOSED
        private set

    var lastEventId = ""
        private set

    var reconnectionTime = 2000L
        private set

    private var call: Call? = null

    private var threadRetry: Thread? = null

    fun connect() {
        close()
        readyState = CONNECTING

        val executor = Executors.newSingleThreadExecutor()
        executor.submit {
            connectInternal()
        }
    }

    fun close() {
        threadRetry?.interrupt()
        readyState = CLOSED
        call?.cancel()
    }

    private fun connectInternal() {
        readyState = CONNECTING

        val client = OkHttpClient.Builder()
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .build()
        val builder = Request.Builder()
                .url(url)
                .addHeader("Accept", "text/event-stream")

        if (!lastEventId.isEmpty()) {
            builder.addHeader("Last-Event-ID", lastEventId)
        }

        call = client.newCall(builder.build())

        call?.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                e?.let {
                    notifyError(it)
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (response != null && response.isSuccessful) {
                    readyState = OPEN
                    notifyOpen()
                    read(response)
                } else {
                    notifyError(IOException(response?.message()))
                }
            }
        })
    }

    private fun read(response: Response) {
        val source = response.body()?.source() ?: return

        val reader = EventSourceReader(source)
        reader.read()
    }

    private fun notifyOpen() {
        eventHandler.onOpen()
    }

    private fun notifyMessage(event: MessageEvent) {
        eventHandler.onMessage(event)
    }

    private fun notifyError(e: Exception) {
        if (readyState == CLOSED) return

        eventHandler.onError(e)

        if (call != null && !call!!.isCanceled) {
            readyState = CONNECTING
            retry()
        } else {
            readyState = CLOSED
            call?.cancel()
        }
    }

    private fun retry() {
        try {
            call?.cancel()
            threadRetry = Thread.currentThread()
            Thread.sleep(reconnectionTime)
            connectInternal()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    internal inner class EventSourceReader(private val source: BufferedSource) {

        private val retryValuePattern = Regex("^\\d+\$")

        internal var event: String? = null

        internal var id: String? = null

        internal var data = mutableListOf<String>()

        fun read() {
            clear()

            val call = call ?: return

            try {

                while (!call.isCanceled) {
                    val line = source.readUtf8LineStrict()

                    if (line.isEmpty()) {
                        dispatchEvent()
                    } else {
                        parse(line)
                    }
                }

            } catch (e: Exception) {
                notifyError(e)
            } finally {
                source.close()
            }

        }

        private fun clear() {
            event = null
            id = null
            data.clear()
        }

        internal fun parse(line: String) {
            var field = line
            var value = ""

            val index = line.indexOf(":")
            if (index == 0) {
                // comment
            } else if (index != -1) {
                field = line.substring(0, index)
                value = line.substring(index + 1).trim()
            }

            when (field) {
                "event" -> event = value
                "data" -> data.add(value)
                "id" -> id = value
                "retry" -> setRetry(value)
            }
        }

        private fun dispatchEvent() {
            if (data.isEmpty()) {
                return
            }

            val event = MessageEvent(event, id, data.joinToString("\n"))
            id?.let { lastEventId = it }
            notifyMessage(event)
            clear()
        }

        private fun setRetry(value: String) {
            if (retryValuePattern.matches(value)) {
                reconnectionTime = value.toLong()
            }
        }
    }
}