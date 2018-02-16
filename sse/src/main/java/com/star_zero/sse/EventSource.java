package com.star_zero.sse;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSource;

public class EventSource {

    public static final int CONNECTING = 0;
    public static final int OPEN = 1;
    public static final int CLOSED = 2;

    @NonNull
    private final String url;

    @Nullable
    private final Map<String, String> header;

    @NonNull
    private final EventHandler eventHandler;

    private long readTimeout = 0;

    private int readyState = CLOSED;

    @NonNull
    private String lastEventId = "";

    private long reconnectionTime = 2000;

    @Nullable
    private Call call;

    @Nullable
    private Thread threadRetry;

    public EventSource(@NonNull String url, @NonNull EventHandler eventHandler) {
        this(url, null, eventHandler);
    }

    public EventSource(@NonNull String url, @Nullable Map<String, String> header, @NonNull EventHandler eventHandler) {
        this.url = url;
        this.header = header;
        this.eventHandler = eventHandler;
    }

    public long getReconnectionTime() {
        return reconnectionTime;
    }

    public String getLastEventId() {
        return lastEventId;
    }

    public int getReadyState() {
        return readyState;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public void connect() {
        close();
        readyState = CONNECTING;

        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                connectInternal();
            }
        });
    }

    public void close() {
        if (threadRetry != null) {
            threadRetry.interrupt();
        }
        if (call != null) {
            call.cancel();
        }
        readyState = CLOSED;
    }

    private void connectInternal() {
        readyState = CONNECTING;
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .build();

        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("Accept", "text/event-stream");

        if (!lastEventId.isEmpty()) {
            builder.addHeader("Last-Event-ID", lastEventId);
        }

        if (header != null) {
            for (Map.Entry<String,String> entry: header.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        call = client.newCall(builder.build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                notifyError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    readyState = OPEN;
                    notifyOpen();
                    read(response);
                } else {
                    if (response != null) {
                        notifyError(new IOException(response.message()));
                    } else {
                        notifyError(new IOException());
                    }
                }
            }
        });
    }

    private void read(Response response) {
        if (response.body() == null) {
            return;
        }

        BufferedSource source = response.body().source();
        if (source == null) {
            return;
        }

        EventSourceReader reader = new EventSourceReader(source);
        reader.read();
    }

    private void notifyOpen() {
        eventHandler.onOpen();
    }

    private void notifyMessage(MessageEvent event) {
        eventHandler.onMessage(event);
    }

    private void notifyError(@Nullable Exception e) {
        if (readyState == CLOSED) {
            return;
        }

        eventHandler.onError(e);

        if (call != null && !call.isCanceled()) {
            readyState = CONNECTING;
            retry();
        } else {
            readyState = CLOSED;
            call.cancel();
        }
    }

    private void retry() {
        try {
            if (call != null) {
                call.cancel();
            }
            threadRetry = Thread.currentThread();
            Thread.sleep(reconnectionTime);
            connectInternal();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class EventSourceReader {
        @NonNull
        BufferedSource source;

        @Nullable
        String event;

        @Nullable
        String id;

        @NonNull
        List<String> data = new ArrayList<>();

        public EventSourceReader(@NonNull BufferedSource source) {
            this.source = source;
        }

        void read() {
            clear();

            if (call == null) {
                return;
            }

            try {
                while (!call.isCanceled()) {
                    String line = source.readUtf8LineStrict();
                    if (line.isEmpty()) {
                        dispatchEvent();
                    } else {
                        parse(line);
                    }
                }
            } catch (IOException e) {
                notifyError(e);
            } finally {
                try {
                    source.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        void clear() {
            event = null;
            id = null;
            data.clear();
        }

        void parse(String line) {
            String field = line;
            String value = "";

            int index = line.indexOf(":");
            if (index == 0) {
                // comment
                return;
            } else if (index != -1) {
                field = line.substring(0, index);
                value = line.substring(index + 1).trim();
            }

            if ("event".equals(field)) {
                event = value;
            } else if ("data".equals(field)) {
                data.add(value);
            } else if ("id".equals(field)) {
                id = value;
            } else if ("retry".equals(field)) {
                setRetry(value);
            }
        }

        void dispatchEvent() {
            if (data.isEmpty()) {
                return;
            }

            MessageEvent event = new MessageEvent(this.event, id, joinData());
            if (id != null) {
                lastEventId = id;
            }
            notifyMessage(event);
            clear();
        }

        String joinData() {
            StringBuilder builder = new StringBuilder();
            for (int i = 0, n = data.size(); i < n; i++) {
                if (i > 0) {
                    builder.append("\n");
                }
                builder.append(data.get(i));
            }
            return builder.toString();
        }

        void setRetry(String value) {
            try {
                reconnectionTime = Long.parseLong(value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

}
