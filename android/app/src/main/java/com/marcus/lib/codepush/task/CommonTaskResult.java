package com.marcus.lib.codepush.task;

import com.marcus.lib.codepush.base.MMErrorCode;

/**
 * Created by marcus on 17/4/29.
 */

class CommonTaskResult {
    private int errorCode;
    private String errorMessage;
    private Object data;

    protected int getErrorCode() {
        return errorCode;
    }

    protected void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    protected String getErrorMessage() {
        return errorMessage;
    }

    protected void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    protected Object getData() {
        return data;
    }

    protected void setData(Object data) {
        this.data = data;
    }

    /*
    Build Method For A Successful Result
     */
    protected static final CommonTaskResult build() {
        CommonTaskResult result = new CommonTaskResult();
        result.setErrorCode(MMErrorCode.SUCCESS);
        result.setErrorMessage("");
        return result;
    }
}

