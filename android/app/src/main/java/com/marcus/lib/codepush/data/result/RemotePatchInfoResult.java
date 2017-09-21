package com.marcus.lib.codepush.data.result;

import com.google.gson.annotations.SerializedName;
import com.marcus.lib.codepush.MMCodePushConstants;
import com.marcus.lib.codepush.util.UPLogUtils;

/*!
 * Copyright(c) 2009-2017 Marcus Ma
 * E-mail:maji1991@sina.com
 * GitHub : https://github.com/MarcusMa
 * MIT Licensed
 */

public class RemotePatchInfoResult {
    @SerializedName(MMCodePushConstants.KEY_HASHCODE)
    private String hashCode;

    @SerializedName(MMCodePushConstants.KEY_DOWNLOAD_URL)
    private String downloadUrl;

    /**
     * getter and setter
     */

    public String getHashCode() {
        return hashCode;
    }

    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void print(int i) {
        StringBuffer prefixBuffer = new StringBuffer();
        for (int j = 0; j < i; j++) {
            prefixBuffer.append("\t");
        }
        UPLogUtils.d(prefixBuffer.toString() + "hashCode : " + hashCode);
        UPLogUtils.d(prefixBuffer.toString() + "downloadUrl : " + downloadUrl);
    }
}
