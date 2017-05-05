package com.marcus.lib.codepush.data.result;

import com.google.gson.annotations.SerializedName;
import com.marcus.lib.codepush.MMCodePushConstants;
import com.marcus.lib.codepush.util.UPLogUtils;

import java.io.Serializable;

/**
 * Created by marcus on 17/4/10.
 */

public class BusinessInfoResult implements Serializable {

    @SerializedName(MMCodePushConstants.KEY_BUSINESS_ID)
    private String id;

    @SerializedName(MMCodePushConstants.KEY_VERIFY_HASHCODE)
    private String verifyHashCode;

    @SerializedName(MMCodePushConstants.KEY_LATEST_PATCH)
    private RemotePatchInfoResult latestPatch;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVerifyHashCode() {
        return verifyHashCode;
    }

    public void setVerifyHashCode(String verifyHashCode) {
        this.verifyHashCode = verifyHashCode;
    }

    public RemotePatchInfoResult getLatestPatch() {
        return latestPatch;
    }

    public void setLatestPatch(RemotePatchInfoResult latestPatch) {
        this.latestPatch = latestPatch;
    }

    public void print(int i) {
        StringBuffer prefixBuffer = new StringBuffer();
        for (int j = 0; j < i; j++) {
            prefixBuffer.append("\t");
        }
        UPLogUtils.d(prefixBuffer.toString() + "id : " + id);
        UPLogUtils.d(prefixBuffer.toString() + "verifyHashCode : " + verifyHashCode);
        if (null != latestPatch) {
            latestPatch.print(i);
        }
        UPLogUtils.d("");
    }
}
