package com.star_zero.sse.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.star_zero.sse.EventHandler
import com.star_zero.sse.EventSource
import com.star_zero.sse.MessageEvent

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName

        // TODO: Replace URL
        private val URL = "https://example.com/sse"
    }

    private var eventSource: EventSource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        eventSource = EventSource(URL, object : EventHandler {
            override fun onOpen() {
                Log.d(TAG, "open")
            }
            override fun onMessage(event: MessageEvent) {
                Log.d(TAG, "message: $event")
            }
            override fun onError(e: Exception) {
                Log.w(TAG, e)
            }
        })

        findViewById<Button>(R.id.button_connect).setOnClickListener({
            Log.d(TAG, "connect")

            eventSource?.connect()
        })

        findViewById<Button>(R.id.button_close).setOnClickListener({
            Log.d(TAG, "close")

            eventSource?.close()
        })
    }
}
