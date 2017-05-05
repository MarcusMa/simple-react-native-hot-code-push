package com.marcus.lib.codepush.manager;

import android.text.TextUtils;

import com.marcus.lib.codepush.MMCodePushConstants;
import com.marcus.lib.codepush.base.BusinessPatch;
import com.marcus.lib.codepush.base.CheckUpdateState;
import com.marcus.lib.codepush.base.UpdateState;
import com.marcus.lib.codepush.data.CheckForUpdateResponse;
import com.marcus.lib.codepush.data.result.BusinessInfoResult;
import com.marcus.lib.codepush.task.MMCheckForUpdateTask;
import com.marcus.lib.codepush.task.MMDownloadTask;
import com.marcus.lib.codepush.util.UPLogUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by marcus on 17/3/23.
 */

public class UpdateManager {

    private static UpdateManager instance;

    public static UpdateManager getInstance() {
        if (null == instance) {
            instance = new UpdateManager();
        }
        return instance;
    }

    private CheckUpdateState mCheckUpdateState = CheckUpdateState.NOT_START;
    private CheckForUpdateCallback mCallback;
    private Map<String, BusinessInfoResult> mBusinessInfoResultMap = new HashMap<>();

    private Map<String, UPDownloadTaskListener> mDownloadListenerMap = new HashMap<>();

    private SettingManager settingManager;

    private UpdateManager() {
        settingManager = SettingManager.getInstance();
    }

    public void checkForUpdate(CheckForUpdateCallback callback) {
        mCallback = callback;
        setCheckUpdateState(CheckUpdateState.WAITING_FOR_RESPONSE);
        try {
            String requestMessage = buildCheckUpdateMessage();
            MMCheckForUpdateTask checkForUpdateTask = new MMCheckForUpdateTask(
                    requestMessage,
                    new MMCheckForUpdateTask.UPCheckForUpdateTaskCallback() {
                        @Override
                        public void onSuccess(CheckForUpdateResponse response) {
                            if (null == response) {
                                setCheckUpdateState(CheckUpdateState.CHECK_UPDATE_FAILED);
                                mCallback.onResult(false, "Server Response is null.");
                            } else if (!response.isSuccess()) {
                                setCheckUpdateState(CheckUpdateState.CHECK_UPDATE_FAILED);
                                mCallback.onResult(false, "Server Error By:" + response.getMsg());
                            } else {
                                setCheckUpdateState(CheckUpdateState.CHECK_UPDATE_SUCCESS);
                                mCallback.onResult(true, "success");
                                processResponse(response);
                            }
                        }

                        @Override
                        public void onError(int errorCode, String errorMsg) {
                            setCheckUpdateState(CheckUpdateState.CHECK_UPDATE_FAILED);
                            mCallback.onResult(false, errorMsg);
                        }
                    });
            MMAsyncTaskManager taskManager = MMAsyncTaskManager.getInstance();
            taskManager.execute(checkForUpdateTask);
        } catch (IOException e) {
            setCheckUpdateState(CheckUpdateState.CHECK_UPDATE_FAILED);
            mCallback.onResult(false, "Execute Remote Task Failed.");
            e.printStackTrace();
        }
    }

    private void processResponse(CheckForUpdateResponse response) {
        List<BusinessInfoResult> businessList = response.getData();
        if (businessList != null && businessList.size() > 0) {
            for (int i = 0; i < businessList.size(); i++) {
                final BusinessInfoResult oneInfo = businessList.get(i);
                //map存储所有返回的BusiInfo,可通过getBusinessInfoById获取对象
                mBusinessInfoResultMap.put(oneInfo.getId(), oneInfo);
                /** 更新本地 Business 的信息 **/
                BusinessManager.getInstance().updateBusinessInfo(oneInfo);
                BusinessPatch localBusinessPatch = BusinessManager.getInstance().getBusinessPackageById(oneInfo.getId());
                if (null != oneInfo.getLatestPatch()) {
                    /** 有升级包的情况下 **/
                    try {
                        MMDownloadTask downloadTask = new MMDownloadTask(localBusinessPatch.getBusinessId(),
                                oneInfo.getLatestPatch().getDownloadUrl(),
                                oneInfo.getLatestPatch().getHashCode(),
                                new MMDownloadTask.UPDownloadTaskCallback() {
                                    @Override
                                    public void onStart(String businessId) {
                                        BusinessManager.getInstance().setUpdateStateById(businessId, UpdateState.UPDATING);
                                    }

                                    @Override
                                    public void onProgress(String businessId, Integer progress) {
                                        // 执行监听器
                                        if (null != mDownloadListenerMap && null != mDownloadListenerMap.get(businessId)) {
                                            mDownloadListenerMap.get(businessId).onBusinessPatchDownloadProgress(progress);
                                        }
                                    }

                                    @Override
                                    public void onSuccess(String businessId, String newHashCode) {
                                        BusinessPatch businessPatch = BusinessManager.getInstance().getBusinessPackageById(businessId);
                                        businessPatch.setLocalHashCode(newHashCode);
                                        BusinessManager.getInstance().setUpdateStateById(businessId, UpdateState.UPDATED_SUCCESS);
                                        // 执行监听器
                                        if (null != mDownloadListenerMap && null != mDownloadListenerMap.get(businessId)) {
                                            mDownloadListenerMap.get(businessId).onBusinessPatchDownloadSuccess();
                                        }
                                    }

                                    @Override
                                    public void onError(String businessId, int errorCode, String errorMsg) {
                                        BusinessManager.getInstance().setUpdateStateById(businessId, UpdateState.UPDATED_FAILED);
                                        if (null != mDownloadListenerMap && null != mDownloadListenerMap.get(businessId)) {
                                            mDownloadListenerMap.get(businessId).onBusinessPatchDownloadError(errorCode, errorMsg);
                                        }
                                    }
                                });

                        MMAsyncTaskManager taskManager = MMAsyncTaskManager.getInstance();
                        taskManager.execute(downloadTask);

                    } catch (Exception e) {
                        BusinessManager.getInstance().setUpdateStateById(oneInfo.getId(), UpdateState.UPDATED_FAILED);
                    }
                }
            }
        }
    }

    /**
     * build post message
     */
    private String buildCheckUpdateMessage() {
        String ret = "";
        Map<String, BusinessPatch> packageMap = BusinessManager.getInstance().getLocalPackageMap();
        if (packageMap == null || packageMap.isEmpty()) {
            return ret;
        }
        JSONObject retObj = new JSONObject();
        try {
            JSONArray array = new JSONArray();
            for (String key : packageMap.keySet()) {
                BusinessPatch tmp = packageMap.get(key);
                JSONObject localBusinessInfo = new JSONObject();
                localBusinessInfo.put(MMCodePushConstants.KEY_BUSINESS_ID, tmp.getBusinessId());
                localBusinessInfo.put(MMCodePushConstants.KEY_LOCAL_PACKAGE_HASH_CODE, tmp.getLocalHashCode());
                array.put(localBusinessInfo);
            }
            retObj.put(MMCodePushConstants.KEY_LOCAL_BUSINESS_LIST, array);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ret = retObj.toString();
        UPLogUtils.d("request msg:" + ret);
        return ret;
    }

    public boolean isCheckUpdateStateSuccess() {
        if (this.mCheckUpdateState == CheckUpdateState.CHECK_UPDATE_SUCCESS) {
            return true;
        }
        return false;
    }

    private void setCheckUpdateState(CheckUpdateState state) {
        UPLogUtils.d("set CheckUpdateState = " + state);
        this.mCheckUpdateState = state;
    }

    public void addDownloadTaskListener(String businessId, UPDownloadTaskListener listener) {
        if (null == mDownloadListenerMap) {
            mDownloadListenerMap = new HashMap<>();
        }
        if (TextUtils.isEmpty(businessId) || null == listener) {
            return;
        }
        mDownloadListenerMap.put(businessId, listener);
    }

    public void removeDownloadTaskListener(String businessId) {
        if (TextUtils.isEmpty(businessId) || null == mDownloadListenerMap) {
            return;
        }
        mDownloadListenerMap.remove(businessId);
    }

    public interface UPDownloadTaskListener {
        void onBusinessPatchDownloadSuccess();

        void onBusinessPatchDownloadProgress(Integer progress);

        void onBusinessPatchDownloadError(int errorCode, String errorMessage);
    }
}
