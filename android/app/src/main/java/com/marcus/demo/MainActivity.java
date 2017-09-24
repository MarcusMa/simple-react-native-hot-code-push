package com.marcus.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.marcus.reactnative.lib.MMCodePush;
import com.marcus.reactnative.lib.MMCodePushConstants;
import com.marcus.reactnative.lib.manager.CheckForUpdateCallback;
import com.marcus.reactnative.lib.util.UPLogUtils;
import com.marcus.rn.demo.demo.R;

/*!
 * Copyright(c) 2009-2017 Marcus Ma
 * E-mail:maji1991@sina.com
 * GitHub : https://github.com/MarcusMa
 * MIT Licensed
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtn;
    private Button mCheckBtn;
    private Context mContext;
    private MMCodePush codePush;

    private static final String TAG = "Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mBtn = (Button) findViewById(R.id.btn_test);
        mCheckBtn = (Button) findViewById(R.id.btn_checkforupdate);
        mCheckBtn.setOnClickListener(this);
        mBtn.setOnClickListener(this);
        mBtn.setEnabled(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        String mServerUrl = "http://192.168.1.3:8888"; // home
        // String mServerUrl = "http://172.20.143.41:8888"; // company
        mBtn.setEnabled(false);
        codePush = new MMCodePush(this, mServerUrl, true);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_test) {
            Intent intent = new Intent(mContext, DemoRNActivity.class);
            intent.putExtra(MMCodePushConstants.KEY_BUSINESS_ID, "AAF047B7-E816-2AE0-949A-D5FB4CE40245");
            mContext.startActivity(intent);
        }
        if (v.getId() == R.id.btn_checkforupdate) {
            UPLogUtils.d(TAG, "check fo update btn clicked.");
            if (null != codePush) {
                codePush.checkForUpdate(new CheckForUpdateCallback() {
                    @Override
                    public void onResult(boolean isSuccess, String errorMessage) {
                        if (isSuccess) {
                            mBtn.setEnabled(true);
                            Toast.makeText(mContext, "CHECK FOR UPDATE SUCCESS.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                Toast.makeText(mContext, "MMCodePush Object is not init. You Should init MMCodePush Object firstly.", Toast.LENGTH_LONG);
            }

        }
    }
}
