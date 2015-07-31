package com.skytech.chatim.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.easemob.chatuidemo.DemoApplication;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.activity.BaseActivity;
import com.skytech.chatim.proxy.ApiAsyncTask;
import com.skytech.chatim.sky.util.AndroidUtil;
import com.skytech.chatim.sky.util.DataUtil;
import com.skytech.chatim.sky.xmlapi.WebexAPIConstant;
import com.skytech.chatim.sky.xmlapi.WebexConfig;


/**
 * Created by jasonc on 2015/5/16.
 */
public class BindWebexActivity extends BaseActivity   {
   private static final String TAG = BindWebexActivity.class.getSimpleName();
    private EditText mUser; // 帐号编辑框
    private EditText mPassword; // 密码编辑框
    private EditText mSite;
    private WebexConfig webexConfig;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sky_activity_webex);
        mUser = (EditText)findViewById(R.id.userName);
        mPassword = (EditText)findViewById(R.id.password);
        mSite = (EditText)findViewById(R.id.webexSite);
        if (DataUtil.isEmpty(DataUtil.readFromPreferences(this, WebexAPIConstant.WBX_USERNAME))){
            mUser.setText(DemoApplication.getInstance().getUserName()+"@tcl.com");
            mPassword.setText("webexgo");
            mSite.setText("demo");
        }else{
            mUser.setText(DataUtil.readFromPreferences(this, WebexAPIConstant.WBX_USERNAME));
            mPassword.setText(DataUtil.readFromPreferences(this, WebexAPIConstant.WBX_PASSWORD));
            mSite.setText(DataUtil.readFromPreferences(this, WebexAPIConstant.WBX_SITE));
        }
    }

    public void ok(View view) {
        String username = mUser.getText().toString();
        String password = mPassword.getText().toString();
        String siteName = mSite.getText().toString();
        webexConfig = new WebexConfig(siteName,username,password);
        ApiAsyncTask apiAsyncTask = new ApiAsyncTask(this , webexConfig);
        apiAsyncTask.execute();
    }

    public void result(Integer result) {
        Log.d(TAG," czqresult" + result) ;
        DataUtil.writeToPreferences(this, WebexAPIConstant.WBX_USERNAME, webexConfig.getUserName());
        DataUtil.writeToPreferences(this, WebexAPIConstant.WBX_PASSWORD, webexConfig.getPassword());
        DataUtil.writeToPreferences(this, WebexAPIConstant.WBX_SITE, webexConfig.getSite());
        if (result > 0){
            AndroidUtil.showToast(this,"绑定成功");
            DataUtil.writeToPreferences(this, WebexAPIConstant.WBX_USERNAME, webexConfig.getUserName());
            DataUtil.writeToPreferences(this, WebexAPIConstant.WBX_PASSWORD, webexConfig.getPassword());
            DataUtil.writeToPreferences(this, WebexAPIConstant.WBX_SITE, webexConfig.getSite());
            setToSkyUser();
            finish();
        }else{
            AndroidUtil.showToast(this,"绑定失败，输入信息错误");
        }
    }

    private void setToSkyUser() {
      // SkyUserManager
        
    }
}