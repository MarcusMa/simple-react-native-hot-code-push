package com.marcus.reactnative.lib.manager;

import android.os.AsyncTask;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*!
 * Copyright(c) 2009-2017 Marcus Ma
 * E-mail:maji1991@sina.com
 * GitHub : https://github.com/MarcusMa
 * MIT Licensed
 */

public class MMAsyncTaskManager {
    private static MMAsyncTaskManager instance;

    private ExecutorService executor;

    public static MMAsyncTaskManager getInstance() {
        if (null == instance) {
            instance = new MMAsyncTaskManager();
        }
        return instance;
    }

    private MMAsyncTaskManager() {
        executor = Executors.newCachedThreadPool();
    }

    public void execute(AsyncTask task, String... params) throws IOException {
        if (null == executor) {
            return; // FIXME
        }
        try {
            if (Build.VERSION.SDK_INT < 11) {
                task.execute(params);
            } else {
                task.executeOnExecutor(executor, params);
            }
        } catch (Exception e) {
            throw new IOException("Execute Task Failed!"); //FIXME
        }
    }
}
