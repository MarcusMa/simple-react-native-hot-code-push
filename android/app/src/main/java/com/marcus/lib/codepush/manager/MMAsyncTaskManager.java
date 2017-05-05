package com.marcus.lib.codepush.manager;

import android.os.AsyncTask;
import android.os.Build;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by marcus on 17/4/28.
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
