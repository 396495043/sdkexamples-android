package com.skytech.chatim.ui;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.chatuidemo.DemoApplication;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.activity.BaseActivity;
import com.easemob.chatuidemo.domain.User;
import com.skytech.chatim.proxy.RetrofitClient;
import com.skytech.chatim.proxy.SkyUserManager;
import com.skytech.chatim.proxy.SkyUserUtils;
import com.skytech.chatim.proxy.UserExtend;
import com.skytech.chatim.sky.retrofit.ServerInterface;
import com.skytech.chatim.sky.util.AndroidUtil;
import com.skytech.chatim.sky.util.DataUtil;
import com.skytech.chatim.sky.vo.SkyUserResponse;
import com.squareup.picasso.Picasso;

public class ContactInfoActivity extends BaseActivity {
    private static String TAG = ContactInfoActivity.class.getSimpleName();
    private ImageView iv_header;
    private ProgressDialog progressDialog;
    private String userName;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        userName = getIntent().getStringExtra(DataUtil.IntentKey);
        setContentView(R.layout.sky_activity_contact);
        ((TextView) findViewById(R.id.tv_username))
                .setText(userName);
        progressDialog = AndroidUtil.getProgressDialog(this, null);
        User user = SkyUserManager.getInstances().getUser(userName);
        Log.d(TAG," userName "+ userName +" user "+ user );
        showData(user);
        progressDialog.show();
        refreshUserInfo();

    }

    private void showData(User user) {
        setEditValue(R.id.tv_nickname,user.getNick());
        UserExtend extend = user.getExtend();
        setEditValue(R.id.tv_email,extend.getEmail());
        setEditValue(R.id.tv_org,extend.getOrg());
        setEditValue(R.id.tv_group,extend.getGroup());
        setEditValue(R.id.tv_workPhone,extend.getWorkPhone());
        setEditValue(R.id.tv_phone,extend.getPhone());
        String avatar = user.getAvatar();
        ImageView iv_header = (ImageView) findViewById(R.id.iv_header);
        if (avatar != null) {
            Picasso.with(this).load(avatar)
                    .placeholder(R.drawable.default_avatar).into(iv_header);
        }

    }
    
    private void setEditValue(int id,String value) {
    	TextView text =	(TextView) findViewById(id);
    	text.setText(value);
	}

    public static void showContact(Activity activity, String username) {
        Intent intent = new Intent(activity, ContactInfoActivity.class);
        intent.putExtra(DataUtil.IntentKey, username);
        activity.startActivity(intent);
    }

    public void refreshUserInfo() {
        final ServerInterface serverInterface = RetrofitClient
                .getServerInterface();
        serverInterface.getSkyUser(userName, new Callback<SkyUserResponse>() {
            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, " getSkyUserByhx  error" + error);
                AndroidUtil.closeDialog(progressDialog);
            }

            @Override
            public void success(SkyUserResponse skyUserResponse, Response arg1) {
                Log.d(TAG, " skyUser  " + skyUserResponse);
                SkyUserUtils.setUserInfo(ContactInfoActivity.this, userName, skyUserResponse.getResult());
                User user = SkyUserManager.getInstances().getUser(userName);
                showData(user);
                AndroidUtil.closeDialog(progressDialog);
            }
        });
    }

}
