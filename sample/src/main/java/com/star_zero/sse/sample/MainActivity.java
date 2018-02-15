package com.star_zero.sse.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.star_zero.sse.EventHandler;
import com.star_zero.sse.EventSource;
import com.star_zero.sse.MessageEvent;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    // replace URL
    private static final String URL = "https://example.com/sse";

    private EventSource eventSource;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eventSource = new EventSource(URL, new EventHandler() {
            @Override
            public void onOpen() {
                // run on worker thread
                Log.d(TAG, "open");
            }

            @Override
            public void onMessage(@NonNull MessageEvent event) {
                // run on worker thread
                Log.d(TAG, "event=" + event.getEvent() +
                        ", id=" + event.getId() + "" +
                        ", data=" + event.getData());
            }

            @Override
            public void onError(@Nullable Exception e) {
                // run on worker thread
                Log.w(TAG, e);
            }
        });

        findViewById(R.id.button_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "connect");
                eventSource.connect();
            }
        });

        findViewById(R.id.button_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "close");
                eventSource.close();
            }
        });
    }
}
