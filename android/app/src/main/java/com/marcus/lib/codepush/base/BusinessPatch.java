package com.marcus.lib.codepush.base;

import android.os.Bundle;

import com.marcus.lib.codepush.MMCodePushConstants;
import com.marcus.lib.codepush.manager.SettingManager;

/**
 * Created by marcus on 17/3/23.
 */

public class BusinessPatch {

    private String businessId;               //业务id
    private String moduleName;              //模块名称
    private Bundle launchOptions;           //组件启动时的附加参数

    private String localHashCode;           //本地业务包的HashCode
    private String verifyHashCode;          //服务器上改业务id对应的HashCode

    private UpdateState updateState;        //更新进度

    private RemotePatchInfo updateInfo;   //远程更新补丁信息

    private BusinessPatch() {
    }

    public BusinessPatch(String buinessId, String localHashCode, String moduleName, Bundle launchOptions) {
        this.businessId = buinessId;
        this.localHashCode = localHashCode;
        this.moduleName = moduleName;
        this.launchOptions = launchOptions;
        this.updateState = UpdateState.NOT_START;
        this.updateInfo = new RemotePatchInfo();
    }

    public BusinessPatch(String businessId, String localHashCode, String moduleName) {
        this(businessId, localHashCode, moduleName, null);
    }

    public String getBusinessId() {
        return businessId;
    }

    public synchronized void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public synchronized void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public Bundle getLaunchOptions() {
        return launchOptions;
    }

    public synchronized void setLaunchOptions(Bundle launchOptions) {
        this.launchOptions = launchOptions;
    }

    public String getLocalHashCode() {
        return localHashCode;
    }

    public synchronized void setLocalHashCode(String localHashCode) {
        this.localHashCode = localHashCode;
    }

    public String getVerifyHashCode() {
        return verifyHashCode;
    }

    public synchronized void setVerifyHashCode(String verifyHashCode) {
        this.verifyHashCode = verifyHashCode;
    }

    public UpdateState getUpdateState() {
        return updateState;
    }

    public synchronized void setUpdateState(UpdateState updateState) {
        this.updateState = updateState;
    }

    public RemotePatchInfo getUpdateInfo() {
        return updateInfo;
    }

    public synchronized void setUpdateInfo(RemotePatchInfo updateInfo) {
        this.updateInfo = updateInfo;
    }

    /**
     * 获取解密、合并完成后的jsbundle的文件路径
     *
     * @return filePath
     */
    public String getBundleFilePath() {
        return SettingManager.getInstance().getBundleFileDir() + "/" + businessId + "." + MMCodePushConstants.DEFAULT_JS_BUNDLE_SUFFIX;
    }

}
