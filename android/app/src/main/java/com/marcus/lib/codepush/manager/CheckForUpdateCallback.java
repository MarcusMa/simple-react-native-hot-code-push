package com.marcus.lib.codepush.manager;

/**
 * Created by marcus on 17/5/4.
 */

public interface CheckForUpdateCallback {
    void onResult(boolean isSuccess, String errorMessage);
}