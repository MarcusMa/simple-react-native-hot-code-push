package com.marcus.reactnative.lib.data;

import com.google.gson.annotations.SerializedName;
import com.marcus.reactnative.lib.MMCodePushConstants;
import com.marcus.reactnative.lib.data.result.BusinessInfoResult;
import com.marcus.reactnative.lib.util.UPLogUtils;

import java.io.Serializable;
import java.util.List;

/*!
 * Copyright(c) 2009-2017 Marcus Ma
 * E-mail:maji1991@sina.com
 * GitHub : https://github.com/MarcusMa
 * MIT Licensed
 */

public class CheckForUpdateResponse implements Serializable {

    private static final int SUCCESS = 1;

    @SerializedName(MMCodePushConstants.KEY_SUCCESS)
    private int success;

    @SerializedName(MMCodePushConstants.KEY_DATA)
    private List<BusinessInfoResult> data;

    @SerializedName(MMCodePushConstants.KEY_MSG)
    private String msg;

    /*
    getter and setter
     */
    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public List<BusinessInfoResult> getData() {
        return data;
    }

    public void setData(List<BusinessInfoResult> data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    /*
    Other
     */
    public boolean isSuccess() {
        return this.success == SUCCESS;
    }

    public void print(int i) {
        StringBuffer prefixBuffer = new StringBuffer();
        for (int j = 0; j < i; j++) {
            prefixBuffer.append("\t");
        }
        UPLogUtils.d(prefixBuffer.toString() + "success : " + String.valueOf(success));
        UPLogUtils.d(prefixBuffer.toString() + "msg : " + msg);
        UPLogUtils.d(prefixBuffer.toString() + "data : ");
        for (BusinessInfoResult tmp : data) {
            if (null != tmp) {
                tmp.print(i + 1);
            }
        }
    }
}
