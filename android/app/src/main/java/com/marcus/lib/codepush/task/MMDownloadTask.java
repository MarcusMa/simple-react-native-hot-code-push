package com.marcus.lib.codepush.task;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.marcus.lib.codepush.MMCodePushConstants;
import com.marcus.lib.codepush.base.MMErrorCode;
import com.marcus.lib.codepush.manager.SettingManager;
import com.marcus.lib.codepush.util.CommonUtils;
import com.marcus.lib.codepush.util.FileUtils;
import com.marcus.lib.codepush.util.UPLogUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by marcus on 17/4/28.
 */

public class MMDownloadTask extends AsyncTask<String, Integer, CommonTaskResult> {

    private static final String TAG = "MMDownloadTask";
    private UPDownloadTaskCallback callback;
    private String downloadUrl;
    private String businessId;
    private String hashCode;

    private MMDownloadTask() {
    }

    public MMDownloadTask(String businessId, String downloadUrl, String hashCode, UPDownloadTaskCallback callback) {
        this.downloadUrl = downloadUrl;
        this.callback = callback;
        this.businessId = businessId;
        this.hashCode = hashCode;
    }

    @Override
    protected void onPreExecute() {
        UPLogUtils.d(TAG, " MMDownloadTask onPreExecute ");
        if (null != callback) {
            callback.onStart(businessId);
        }
        super.onPreExecute();
    }

    @Override
    protected CommonTaskResult doInBackground(String... params) {
        CommonTaskResult ret = CommonTaskResult.build();
        if (TextUtils.isEmpty(downloadUrl)) {
            ret.setErrorCode(MMErrorCode.PARAMS_ERROR);
            ret.setErrorMessage("patch download url should be empty.");
            return ret;
        }

        SettingManager settingManager = SettingManager.getInstance();
        if (null == settingManager) {
            ret.setErrorCode(MMErrorCode.NEED_INIT_FIRST_ERROR);
            ret.setErrorMessage("Can not get Custom Settings");
            return ret;
        }

        String realDownloadUrl = "";
        if (downloadUrl.startsWith("http")) {
            realDownloadUrl = downloadUrl;
        } else {
            realDownloadUrl = settingManager.getServerUrl() + "/" + downloadUrl;
        }
        UPLogUtils.d("download patch file for : " + businessId + " url: " + realDownloadUrl);

        /*
        start to download patch
         */
        HttpURLConnection connection = null;
        BufferedInputStream bin = null;
        FileOutputStream fos = null;
        BufferedOutputStream bout = null;
        File downloadFile = null;

        try {
            URL downloadUrl = new URL(realDownloadUrl);
            connection = (HttpURLConnection) (downloadUrl.openConnection());

            long totalBytes = connection.getContentLength();
            long receivedBytes = 0;
            bin = new BufferedInputStream(connection.getInputStream());

            /*
            先删除 用于存放 下载文件的临时目录
            */
            String tmpFileRootDir = FileUtils.appendPathComponent(settingManager.getBundleFileTmpDir(), businessId);
            File downloadFolder = new File(tmpFileRootDir);
            if (downloadFolder.isDirectory()) {
                downloadFolder.delete();
            }
            downloadFolder.mkdirs();

            downloadFile = new File(downloadFolder, MMCodePushConstants.DEFAULT_DOWNLOAD_PATCH_NAME);
            fos = new FileOutputStream(downloadFile);
            bout = new BufferedOutputStream(fos, MMCodePushConstants.DOWNLOAD_BUFFER_SIZE);
            byte[] data = new byte[MMCodePushConstants.DOWNLOAD_BUFFER_SIZE];
            byte[] header = new byte[4];
            int numBytesRead = 0;
            while ((numBytesRead = bin.read(data, 0, MMCodePushConstants.DOWNLOAD_BUFFER_SIZE)) >= 0) {
                if (receivedBytes < 4) {
                    for (int i = 0; i < numBytesRead; i++) {
                        int headerOffset = (int) (receivedBytes) + i;
                        if (headerOffset >= 4) {
                            break;
                        }
                        header[headerOffset] = data[i];
                    }
                }
                receivedBytes += numBytesRead;
                bout.write(data, 0, numBytesRead);
            }
            UPLogUtils.e("Received " + receivedBytes + " bytes, expected " + totalBytes);
            ret.setData(downloadFile.getAbsolutePath());
        } catch (Exception e) {
            ret.setErrorCode(MMErrorCode.URL_ERROR);
            ret.setErrorMessage("Invalid downloadUrl");
            UPLogUtils.e("The package has an invalid downloadUrl: " + downloadUrl);
            e.printStackTrace();
        } finally {
            try {
                if (bout != null) bout.close();
                if (fos != null) fos.close();
                if (bin != null) bin.close();
                if (connection != null) connection.disconnect();
            } catch (IOException e) {
                ret.setErrorCode(MMErrorCode.IO_ERROR);
                ret.setErrorMessage("Error closing IO resources.");
                UPLogUtils.e("Error closing IO resources.");
                e.printStackTrace();
            }
        }
        if (MMErrorCode.SUCCESS == ret.getErrorCode()) {
            try {
                /*
                检查文件合法性
                */
                String shaStr = CommonUtils.computeFileHash(downloadFile);
                if (shaStr.equalsIgnoreCase(hashCode)) {
                    /*
                    Move file from tmp dir to real dir
                     */
                    UPLogUtils.i("check download file success, now to move the file");
                    String realFileRootDir = FileUtils.appendPathComponent(settingManager.getBundleFileDir(), businessId);
                    FileUtils.moveFile(downloadFile, realFileRootDir, MMCodePushConstants.DEFAULT_BUSINESS_PATCH_NAME);
                } else {
                    ret.setErrorCode(MMErrorCode.CHECK_HASH_ERROR);
                    ret.setErrorMessage("check hash of download patch file failed");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                ret.setErrorCode(MMErrorCode.CHECK_HASH_ERROR);
                ret.setErrorMessage("download patch not found");
            }
        }
        return ret;
    }

    @Override
    protected void onPostExecute(CommonTaskResult result) {
        super.onPostExecute(result);
        if (null != callback) {
            if (MMErrorCode.SUCCESS == result.getErrorCode()) {
                callback.onSuccess(businessId, this.hashCode);
            } else {
                callback.onError(businessId, result.getErrorCode(), result.getErrorMessage());
            }
        }
    }

    public interface UPDownloadTaskCallback {
        void onStart(String businessId);

        void onProgress(String businessId, Integer progress);

        void onSuccess(String businessId, String newHashCode);

        void onError(String businesddId, int errorCode, String errorMsg);
    }
}
