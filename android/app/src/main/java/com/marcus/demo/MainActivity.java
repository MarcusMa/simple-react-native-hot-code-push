package com.marcus.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.marcus.lib.codepush.MMCodePush;
import com.marcus.lib.codepush.MMCodePushConstants;
import com.marcus.lib.codepush.manager.CheckForUpdateCallback;
import com.marcus.rn.demo.demo.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtn;
    private Context mContext;
    private MMCodePush codePush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mBtn = (Button) findViewById(R.id.btn_test);
        mBtn.setOnClickListener(this);
        mBtn.setEnabled(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // String mServerUrl = "http://192.168.0.109:8888"; // home
        String mServerUrl = "http://172.20.143.41:8888"; // company
        mBtn.setEnabled(false);
        codePush = new MMCodePush(this, mServerUrl, true);
        codePush.checkForUpdate(new CheckForUpdateCallback() {
            @Override
            public void onResult(boolean isSuccess, String errorMessage) {
                if (isSuccess) {
                    mBtn.setEnabled(true);
                    Toast.makeText(mContext, "检查更新成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_test) {
            Intent intent = new Intent(mContext, DemoRNActivity.class);
            intent.putExtra(MMCodePushConstants.KEY_BUSINESS_ID, "AAF047B7-E816-2AE0-949A-D5FB4CE40245");
            mContext.startActivity(intent);
        }
    }
}
