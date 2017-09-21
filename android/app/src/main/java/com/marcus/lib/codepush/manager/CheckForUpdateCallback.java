package com.marcus.lib.codepush.manager;

/*!
 * Copyright(c) 2009-2017 Marcus Ma
 * E-mail:maji1991@sina.com
 * GitHub : https://github.com/MarcusMa
 * MIT Licensed
 */

public interface CheckForUpdateCallback {
    void onResult(boolean isSuccess, String errorMessage);
}