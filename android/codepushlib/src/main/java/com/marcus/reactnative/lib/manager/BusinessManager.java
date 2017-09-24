package com.marcus.reactnative.lib.manager;

import android.text.TextUtils;

import com.marcus.reactnative.lib.MMCodePushConstants;
import com.marcus.reactnative.lib.base.BusinessPatch;
import com.marcus.reactnative.lib.base.RemotePatchInfo;
import com.marcus.reactnative.lib.base.UpdateState;
import com.marcus.reactnative.lib.data.result.BusinessInfoResult;
import com.marcus.reactnative.lib.util.CommonUtils;
import com.marcus.reactnative.lib.util.FileUtils;
import com.marcus.reactnative.lib.util.UPLogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/*!
 * Copyright(c) 2009-2017 Marcus Ma
 * E-mail:maji1991@sina.com
 * GitHub : https://github.com/MarcusMa
 * MIT Licensed
 */

public class BusinessManager {

    private static BusinessManager instance;

    private Map<String, BusinessPatch> localPackageMap;

    public static BusinessManager getInstance() {
        if (null == instance) {
            instance = new BusinessManager();
        }
        return instance;
    }

    private BusinessManager() {
        init();
    }

    private void init() {
        if (null == localPackageMap) {
            localPackageMap = new HashMap<>();
        }
        localPackageMap.clear();
        String businessDirPatch = SettingManager.getInstance().getBundleFileDir();
        File rootDir = new File(businessDirPatch);
        if (!rootDir.exists()) {
            UPLogUtils.e("create RN business Root Dir");
        } else {
            if (!rootDir.isDirectory()) {
                UPLogUtils.e("clear file and mkdir ");
                rootDir.delete();
                rootDir.mkdir();
            } else {
                UPLogUtils.e("get all files ");
                File[] fileList = rootDir.listFiles();
                for (int i = 0; i < fileList.length; i++) {
                    File tmp = fileList[i];
                    if (tmp.isDirectory() && null != tmp.listFiles() && tmp.listFiles().length == 1) {
                        String businessId = tmp.getName();
                        String shaStr = "";
                        File[] businsessFileList = tmp.listFiles();
                        try {
                            // compute sha-256
                            File businessFile = businsessFileList[0];
                            shaStr = CommonUtils.computeFileHash(businessFile);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        if (!TextUtils.isEmpty(shaStr)) {
                            BusinessPatch tmpBusinessPatch = new BusinessPatch(businessId, shaStr, "WithReactNative");
                            localPackageMap.put(tmpBusinessPatch.getBusinessId(), tmpBusinessPatch);
                        }
                    }
                }
            }
        }
    }

    public BusinessPatch getBusinessPackageById(String id) {
        if (TextUtils.isEmpty(id)) {
            return null;
        }
        return localPackageMap.get(id);
    }

    public boolean hasBusiness(String id) {
        if (TextUtils.isEmpty(id)) {
            return false;
        }
        return null != getBusinessPackageById(id);
    }

    public Map<String, BusinessPatch> getLocalPackageMap() {
        return localPackageMap;
    }

    public void setUpdateStateById(String id, UpdateState state) {
        BusinessPatch businessPatch = getBusinessPackageById(id);
        if (null != businessPatch) {
            businessPatch.setUpdateState(state);
        }
    }

    public void addBusinessPackage(BusinessPatch businessPatch) {
        if (null == businessPatch) {
            return;
        } else {
            localPackageMap.put(businessPatch.getBusinessId(), businessPatch);
        }
    }

    public void updateBusinessInfo(BusinessInfoResult oneInfo) {
        if (null == oneInfo) {
            UPLogUtils.e("Call updateBusinessInfo() with a null params");
            return;
        }
        BusinessPatch tmpPackage = getBusinessPackageById(oneInfo.getId());
        if (null == tmpPackage) {
            tmpPackage = new BusinessPatch(oneInfo.getId(), "", "WithReactNative");
            addBusinessPackage(tmpPackage);
        } else {
            tmpPackage.setLocalHashCode(oneInfo.getVerifyHashCode());
        }
        if (null != oneInfo.getLatestPatch()) {
            RemotePatchInfo updateInfo = new RemotePatchInfo();
            updateInfo.initWithResult(oneInfo.getLatestPatch());
            tmpPackage.setUpdateInfo(updateInfo);
        }

    }

    public String getBusinessPatchPath(String buinessId) {
        if (TextUtils.isEmpty(buinessId)) {
            return "";
        }
        if (localPackageMap.containsKey(buinessId)) {
            String businessRootPath = SettingManager.getInstance().getBundleFileDir();
            String businessFolderPath = FileUtils.appendPathComponent(businessRootPath, buinessId);
            return FileUtils.appendPathComponent(businessFolderPath, MMCodePushConstants.DEFAULT_BUSINESS_PATCH_NAME);
        }
        return "";
    }

}

