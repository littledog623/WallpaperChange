package com.example.hojiang.wallpaperchange;

import android.os.Handler;
import android.os.HandlerThread;

public class MyHandlerThread extends HandlerThread {

    private Handler mWorkerHandler;

    public MyHandlerThread(String name) {
        super(name);
    }

    public MyHandlerThread(String name, int priority) {
        super(name, priority);
    }

    public void postTask(Runnable task) {
        mWorkerHandler.post(task);
    }

    public void prepareHandler() {
        mWorkerHandler = new Handler(getLooper());
    }
}
