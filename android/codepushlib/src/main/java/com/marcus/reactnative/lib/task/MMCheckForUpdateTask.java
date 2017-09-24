package com.marcus.reactnative.lib.task;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.marcus.reactnative.lib.MMCodePushConstants;
import com.marcus.reactnative.lib.base.MMErrorCode;
import com.marcus.reactnative.lib.data.CheckForUpdateResponse;
import com.marcus.reactnative.lib.manager.SettingManager;
import com.marcus.reactnative.lib.util.UPLogUtils;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.marcus.reactnative.lib.util.CommonUtils.streamToString;

/*!
 * Copyright(c) 2009-2017 Marcus Ma
 * E-mail:maji1991@sina.com
 * GitHub : https://github.com/MarcusMa
 * MIT Licensed
 */

public class MMCheckForUpdateTask extends AsyncTask<String, Integer, CommonTaskResult> {

    private static final String TAG = "MMCheckForUpdateTask";
    private static int DEFAULT_CONNECTION_TIMEOUT = 10 * 1000;

    private UPCheckForUpdateTaskCallback callback;
    private String requestMsg = null;

    private MMCheckForUpdateTask() {
    }

    public MMCheckForUpdateTask(String requestMsg, UPCheckForUpdateTaskCallback callback) {
        this.callback = callback;
        this.requestMsg = requestMsg;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        UPLogUtils.d(TAG, " MMCheckForUpdateTask onPreExecute ");
    }

    @Override
    protected CommonTaskResult doInBackground(String... params) {
        CommonTaskResult ret = CommonTaskResult.build();
        SettingManager settingManager = SettingManager.getInstance();
        if (null == settingManager) {
            ret.setErrorCode(MMErrorCode.NEED_INIT_FIRST_ERROR);
            ret.setErrorMessage("Can not get Custom Settings");
            return ret;
        }

        String serverUrl = settingManager.getServerUrl();
        if (TextUtils.isEmpty(serverUrl)) {
            ret.setErrorCode(MMErrorCode.PARAMS_ERROR);
            ret.setErrorMessage("Server Url should not be empty");
            return ret;
        }

        try {
            URL url = new URL(serverUrl + "/" + MMCodePushConstants.CHECK_FOR_UPDATE_INTERFACE);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            OutputStream out = new BufferedOutputStream(conn.getOutputStream());
            UPLogUtils.d(TAG, "Request:" + requestMsg);
            out.write(requestMsg.getBytes());
            out.flush();
            out.close();
            //响应码
            int code = conn.getResponseCode();
            if (code == 200) {
                InputStream inputStream = conn.getInputStream();
                String result = streamToString(inputStream);
                UPLogUtils.d(TAG, " Response : " + result);
                ret.setData(result);
            } else {
                ret.setErrorCode(MMErrorCode.NETWORK_RESPONSE_ERROR);
                ret.setErrorMessage("Server Response Code is " + String.valueOf(code));
            }
        } catch (Exception e) {
            ret.setErrorCode(MMErrorCode.NETWORK_ERROR);
            ret.setErrorMessage(e.getLocalizedMessage());
            e.printStackTrace();
        } finally {
            return ret;
        }
    }

    @Override
    protected void onPostExecute(CommonTaskResult result) {
        if (callback != null) {
            if (result.getErrorCode() == MMErrorCode.SUCCESS) {
                UPLogUtils.d(TAG, "CheckForUpdate Result:" + result.toString());
                try {
                    String response = (String) result.getData();
                    Gson mGson = new Gson();
                    CheckForUpdateResponse checkForUpdateResponse = mGson.fromJson(response, new TypeToken<CheckForUpdateResponse>() {
                    }.getType());
//                    checkForUpdateResponse.print(1); //Just For Print Result
                    callback.onSuccess(checkForUpdateResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onError(MMErrorCode.NETWORK_RESPONSE_ERROR, e.getMessage());
                }
            } else {
                callback.onError(result.getErrorCode(), result.getErrorMessage());
            }
        }
    }

    public interface UPCheckForUpdateTaskCallback {
        void onSuccess(CheckForUpdateResponse response);

        void onError(int errorCode, String errorMsg);
    }
}
