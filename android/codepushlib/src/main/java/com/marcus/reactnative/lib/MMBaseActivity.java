package com.marcus.reactnative.lib;

import android.app.Activity;
import android.content.Context;
import android.content.MutableContextWrapper;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.shell.MainReactPackage;
import com.marcus.reactnative.lib.base.BusinessPatch;
import com.marcus.reactnative.lib.manager.BusinessManager;
import com.marcus.reactnative.lib.manager.UpdateManager;
import com.marcus.reactnative.lib.util.CommonUtils;
import com.marcus.reactnative.lib.util.FileUtils;
import com.marcus.reactnative.lib.util.UPLogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

/*!
 * Copyright(c) 2009-2017 Marcus Ma
 * E-mail:maji1991@sina.com
 * GitHub : https://github.com/MarcusMa
 * MIT Licensed
 */

public class MMBaseActivity extends Activity implements DefaultHardwareBackBtnHandler, UpdateManager.UPDownloadTaskListener {

    private static final String TAG = "MMBaseActivity";
    /*
    Handler Message
     */
    private static final int MSG_HIDE_LOADING_VIEW = 1;
    private static final int MSG_UPDATE_LOADING_VIEW_MSG = 2;
    private static final int MSG_RELOAD_REACT_ROOT_VIEW = 3;

    /*
    Manager or Service
     */
    private UpdateManager updateManager = UpdateManager.getInstance();

    /*
    Data
     */
    private static final int DEFAULT_LOADING_VIEW_APPEAR_DURATION = 600;
    private BusinessPatch currentPackage;
    private Context mContext;
    private boolean isLoadReactViewError;
    private String mLoadReactViewErrorMsg;
    private boolean isWaitingReactViewLoad;
    private ReactInstanceManager mReactInstanceManager;
    private ReactRootView mReactRootView;
    private Timer timer = new Timer();
    private TimerTask hideLoadingViewTask = new TimerTask() {
        @Override
        public void run() {
            handler.sendEmptyMessage(MSG_HIDE_LOADING_VIEW);
        }
    };

    /*
    UI Reference
     */
    private FrameLayout mRootView;
    private LinearLayout mLoadingView;
    private TextView loadMsgLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        isLoadReactViewError = false;
        mLoadReactViewErrorMsg = "";
        isWaitingReactViewLoad = true;
        /**
         *  检查 调用是否非法,包括：
         *  1. 完成CheckForUpdate调用，
         *  2. BusinessId 是否合法、对应的BusinessPackage对象是否存在
         *  */
        BusinessManager packageManager = BusinessManager.getInstance();
        String businessId = getIntent().getStringExtra(MMCodePushConstants.KEY_BUSINESS_ID);
        if (!updateManager.isCheckUpdateStateSuccess() || TextUtils.isEmpty(businessId)
                || null == packageManager || !packageManager.hasBusiness(businessId)) {
            isLoadReactViewError = true;
            mLoadReactViewErrorMsg = "非法调用";
        }

        if (!isLoadReactViewError) {
            currentPackage = packageManager.getBusinessPackageById(businessId);
            if (TextUtils.isEmpty(currentPackage.getLocalHashCode())) {
                UPLogUtils.d(TAG, ">>>>>> no patch file to use, waiting download file complete. <<<<<<<<");
                updateManager.addDownloadTaskListener(currentPackage.getBusinessId(), this);
            } else {
                // 本地有包
                isLoadReactViewError = buildReactViewAsNormal();
            }
        }
        /**
         * 根据 包的情况 设置页面显示内容
         */
        if (isLoadReactViewError) {
            // 有错误
            setContentView(R.layout.activity_loading);
            loadMsgLabel = (TextView) findViewById(R.id.loading_msg);
            loadMsgLabel.setText(mLoadReactViewErrorMsg);
        } else {
            // 有ReactNative的RootView
            mRootView = new FrameLayout(this);
            mLoadingView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.activity_loading, null);
            if (null != mReactRootView) {
                mRootView.addView(mReactRootView);
            }
            mRootView.addView(mLoadingView, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            setContentView(mRootView);
            loadMsgLabel = (TextView) findViewById(R.id.loading_msg);

            //有 ReactRootView 的情况，在加载一段时间后去除 loading view
            if (!isWaitingReactViewLoad) {
                if (timer == null) {
                    timer = new Timer();
                }
                timer.schedule(hideLoadingViewTask, DEFAULT_LOADING_VIEW_APPEAR_DURATION);
            }
        }
    }

    private boolean buildReactViewAsNormal() {
        Boolean retError = false;
        boolean success = rebuildJSBundle(currentPackage);
        if (success) {
            mReactRootView = new ReactRootView(new MutableContextWrapper(this));
            mReactInstanceManager = ReactInstanceManager.builder()
                    .setApplication(getApplication())
                    .setCurrentActivity(this)
                    .setDefaultHardwareBackBtnHandler(this)
                    .setJSBundleFile(currentPackage.getBundleFilePath())
                    .addPackage(new MainReactPackage())
                    .setUseDeveloperSupport(false)
                    .setInitialLifecycleState(LifecycleState.RESUMED)
                    .build();
            mReactInstanceManager.addReactInstanceEventListener(new ReactInstanceManager.ReactInstanceEventListener() {
                @Override
                public void onReactContextInitialized(ReactContext context) {
                    UPLogUtils.d("delete file :" + currentPackage.getBundleFilePath());
                    FileUtils.deleteFileAtPathSilently(currentPackage.getBundleFilePath());
                }
            });
            mReactRootView.startReactApplication(mReactInstanceManager, currentPackage.getModuleName(), null);
            isWaitingReactViewLoad = false;
        } else {
            mReactRootView = null;
            mLoadReactViewErrorMsg = "Error in rebuild bundle file with bsdiff";
            retError = true;
        }
        return retError;
    }

    private void reloadReactRootView() {
        UPLogUtils.d(TAG, ">> reload the react RootView.");
        isLoadReactViewError = buildReactViewAsNormal();
        if (isLoadReactViewError) {
            Message msg = Message.obtain();
            msg.what = MSG_UPDATE_LOADING_VIEW_MSG;
            msg.obj = "加载失败:创建View失败";
            handler.sendMessage(msg);
        } else {
            setContentView(mReactRootView);
        }
    }

    public void hideLoadingView() {
        if (null != mRootView) {
            AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setDuration(500);
            mLoadingView.startAnimation(fadeOut);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mRootView.removeView(mLoadingView);
                    mLoadingView = null;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostResume(this, this);
        }

        Log.e("Time", "onResume :" + System.currentTimeMillis());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostDestroy();
        }
        /**
         * 清理资源
         */
        if (mReactRootView != null) {
            try {
                ViewGroup parent = (ViewGroup) mReactRootView.getParent();
                if (parent != null) {
                    parent.removeView(mReactRootView);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            mReactRootView.unmountReactApplication();
            mReactRootView = null;
        }
        if (null != currentPackage) {
            updateManager.removeDownloadTaskListener(currentPackage.getBusinessId());
        }
        if (null != timer) {
            timer.cancel();
            timer = null;
        }
        if (null != handler) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onBackPressed() {
        if (mReactInstanceManager != null) {
            mReactInstanceManager.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && mReactInstanceManager != null) {
            mReactInstanceManager.showDevOptionsDialog();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    /*
    Handler Event
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HIDE_LOADING_VIEW:
                    hideLoadingView();
                    break;
                case MSG_UPDATE_LOADING_VIEW_MSG:
                    if (null != loadMsgLabel) {
                        loadMsgLabel.setText((String) msg.obj);
                    }
                    break;
                case MSG_RELOAD_REACT_ROOT_VIEW:
                    reloadReactRootView();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    /*
    Business Patch Download Listener
     */
    @Override
    public void onBusinessPatchDownloadSuccess() {
        UPLogUtils.d(TAG, ">> patch file download successful.");
        handler.sendEmptyMessage(MSG_RELOAD_REACT_ROOT_VIEW);
    }

    @Override
    public void onBusinessPatchDownloadProgress(Integer progress) {
        // 暂时不用
    }

    @Override
    public void onBusinessPatchDownloadError(int errorCode, String errorMessage) {
        String alertMsg = "错误：" + errorMessage + "[" + errorCode + "]";
        Message msg = Message.obtain();
        msg.what = MSG_UPDATE_LOADING_VIEW_MSG;
        msg.obj = alertMsg;
        handler.sendMessage(msg);
    }

    public boolean rebuildJSBundle(BusinessPatch businessPatch) {
        if (null == businessPatch) {
            return false;
        }
        BusinessManager packageManager = BusinessManager.getInstance();
        String patchFilePatch = packageManager.getBusinessPatchPath(businessPatch.getBusinessId());
        File patchFile = new File(patchFilePatch);
        if (!patchFile.exists() || !patchFile.isFile() || !patchFile.canRead()) {
            return false;
        } else {
            try {
                String shaStr = CommonUtils.computeFileHash(patchFile);
                if (null != shaStr && shaStr.equalsIgnoreCase(businessPatch.getLocalHashCode())) {
                    InputStream patchInputStream = new FileInputStream(patchFile);
                    InputStream commonInputStream = getClass().getResourceAsStream(MMCodePushConstants.DEFAULT_COMMON_PATCH_PATCH);
                    byte[] commonBytes = FileUtils.readFile(commonInputStream);
                    byte[] patchBytes = FileUtils.readFile(patchInputStream);
                    byte[] outputBytes = CommonUtils.patchMem(commonBytes, patchBytes);
                    FileOutputStream fos = new FileOutputStream(businessPatch.getBundleFilePath());
                    fos.write(outputBytes);
                    fos.close();
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

}
