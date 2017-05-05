package com.marcus.lib.codepush;

import android.app.Activity;
import android.os.Environment;

import com.marcus.lib.codepush.manager.BusinessManager;
import com.marcus.lib.codepush.manager.CheckForUpdateCallback;
import com.marcus.lib.codepush.manager.SettingManager;
import com.marcus.lib.codepush.manager.UpdateManager;
import com.marcus.lib.codepush.util.UPLogUtils;

/**
 * Created by marcus on 17/3/23.
 */

public class MMCodePush {

    private String serviceUrl;
    private boolean debugFlag;
    private Activity context;

    private UpdateManager updateManager;
    private BusinessManager businessManager;

    /**
     * 热更对象
     *
     * @param context    应用的上下文，主业务流程的Activity
     * @param serviceUrl 热更新服务器访问地址
     * @param isDebug    是否开启debug模式，用于打印日志信息
     */
    public MMCodePush(Activity context, String serviceUrl, boolean isDebug) {
        this.serviceUrl = serviceUrl;
        this.debugFlag = isDebug;
        this.context = context;

        // init all managers
        SettingManager settingManager = SettingManager.getInstance();
        // 默认的地址环境是 context.getFilesDir().getAbsolutePath()
        settingManager.setStoreRootDir(context.getFilesDir().getAbsolutePath());
        //settingManager.setStoreRootDir(Environment.getExternalStorageDirectory().getAbsolutePath());
        settingManager.setServerUrl(serviceUrl);
        // this.updateManager = new UpdateManager(context.getFilesDir().getAbsolutePath());
        this.updateManager = UpdateManager.getInstance();
        this.businessManager = BusinessManager.getInstance();

        UPLogUtils.isDebug = isDebug;
    }

    // 检查更新

    /**
     * 检查业务更新方法
     *
     * @param callback 执行的回调函数
     */
    public void checkForUpdate(CheckForUpdateCallback callback) {
        this.updateManager.checkForUpdate(callback);
    }
}
