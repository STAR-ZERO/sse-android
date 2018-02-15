package com.star_zero.sse;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface EventHandler {
    void onOpen();

    void onMessage(@NonNull MessageEvent event);

    void onError(@Nullable Exception e);
}
