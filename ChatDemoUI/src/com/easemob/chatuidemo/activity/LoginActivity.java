/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.easemob.chatuidemo.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMGroupManager;
import com.easemob.chatuidemo.BuildConfig;
import com.easemob.chatuidemo.Constant;
import com.easemob.chatuidemo.DemoApplication;
import com.easemob.chatuidemo.DemoHXSDKHelper;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.db.UserDao;
import com.easemob.chatuidemo.domain.User;
import com.easemob.chatuidemo.utils.CommonUtils;
import com.easemob.exceptions.EaseMobException;
import com.skytech.chatim.proxy.RetrofitClient;
import com.skytech.chatim.proxy.SkyUserManager;
import com.skytech.chatim.proxy.SkyUtil;
import com.skytech.chatim.sky.retrofit.ServerInterface;
import com.skytech.chatim.sky.util.AndroidUtil;
import com.skytech.chatim.sky.util.DataUtil;
import com.skytech.chatim.sky.vo.LoginEasemobResponse;

/**
 * 登陆页面
 * 
 */
public class LoginActivity extends BaseActivity {
	private static final String TAG = "LoginActivity";
	public static final int REQUEST_CODE_SETNICK = 1;
	private EditText usernameEditText;
	private EditText passwordEditText;

	private boolean progressShow;
	private boolean autoLogin = false;

	private String currentUsername;
	private String currentPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 如果用户名密码都有，直接进入主页面
		//SKYMODIFY forbid old hx  login
		if (DemoHXSDKHelper.getInstance().isLogined() && SkyUserManager.getInstances().isAllowHxAutoLogin()) {
			autoLogin = true;
			startActivity(new Intent(LoginActivity.this, MainActivity.class));

			return;
		}
		setContentView(R.layout.activity_login);

		usernameEditText = (EditText) findViewById(R.id.username);
		passwordEditText = (EditText) findViewById(R.id.password);

		// 如果用户名改变，清空密码
		usernameEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				//passwordEditText.setText(null);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		if (DemoApplication.getInstance().getUserName() != null) {
			usernameEditText.setText(DemoApplication.getInstance().getUserName());
		}
		
		//SKYMODIFY aoto login 
		usernameEditText.setText(SkyUserManager.getInstances().getUserName());
		passwordEditText.setText(SkyUserManager.getInstances().getPassword());
		
		if (getIntent().getStringExtra(DataUtil.IntentKey)!=null){     
		    // cong splashActivity 自动登录
		    currentUsername = usernameEditText.getText().toString().trim();
	        currentPassword = passwordEditText.getText().toString().trim();
	        if (!TextUtils.isEmpty(currentUsername) && !TextUtils.isEmpty(currentPassword) ){
	            login(null); 
	        }else{
	        	if (BuildConfig.DEBUG){
		        	usernameEditText.setText("zhongqi.chen");
		    		passwordEditText.setText("Pass1234");
	        	}
	        }
		}

	}

	/**
	 * 登录
	 * 
	 * @param view
	 */
	public void login(View view) {
		if (!CommonUtils.isNetWorkConnected(this)) {
			Toast.makeText(this, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
			return;
		}
		currentUsername = usernameEditText.getText().toString().trim();
		currentPassword = passwordEditText.getText().toString().trim();

		if (TextUtils.isEmpty(currentUsername)) {
			Toast.makeText(this, R.string.User_name_cannot_be_empty, Toast.LENGTH_SHORT).show();
			return;
		}
		if (TextUtils.isEmpty(currentPassword)) {
			Toast.makeText(this, R.string.Password_cannot_be_empty, Toast.LENGTH_SHORT).show();
			return;
		}

		progressShow = true;
		final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
		pd.setCanceledOnTouchOutside(false);
		pd.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				progressShow = false;
			}
		});
		pd.setMessage(getString(R.string.Is_landing));
		pd.show();

		final long start = System.currentTimeMillis();


		//SKYMODIFY two step login ,you need login sky first ,then you get hx userID to login hx .
		final ServerInterface serverInterface = RetrofitClient.getServerInterface();
		 Log.d(TAG," login  currentUsername " + currentUsername) ;
		serverInterface.loginEasemob(currentUsername, currentPassword, new Callback<LoginEasemobResponse>(){

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG," login  error" + error ) ;
                AndroidUtil.showToast(LoginActivity.this, R.string.login_error);
                pd.dismiss();
            }

            @Override
            public void success(LoginEasemobResponse loginResponse , Response arg1) {
                Log.d(TAG," login  loginResponse" + loginResponse ) ;
                SkyUserManager.getInstances().setToken(loginResponse.getResult().getToken());
                SkyUserManager.getInstances().setSkyUser(loginResponse.getResult());
                hxUserName = loginResponse.getResult().getUid();
                hxPassword =loginResponse.getResult().getPassword();
                hxLogin(pd);
            }
		    });
	}

	private String hxUserName ;
	private String hxPassword ;	
	// spit two step to login
	protected void hxLogin(final ProgressDialog pd) {
			

		// 调用sdk登陆方法登陆聊天服务器
		EMChatManager.getInstance().login(hxUserName, hxPassword, new EMCallBack() {

			@Override
			public void onSuccess() {
				if (!progressShow) {
					return;
				}
				// 登陆成功，保存用户名密码
				DemoApplication.getInstance().setUserName(currentUsername);
				DemoApplication.getInstance().setPassword(currentPassword);
             	//SKYMODIFY
 				SkyUserManager.getInstances().save(currentUsername,currentPassword);
				try {
					// ** 第一次登录或者之前logout后再登录，加载所有本地群和回话
					// ** manually load all local groups and
				    EMGroupManager.getInstance().loadAllGroups();
					EMChatManager.getInstance().loadAllConversations();
					// 处理好友和群组
					initializeContacts();
				} catch (Exception e) {
					e.printStackTrace();
					// 取好友或者群聊失败，不让进入主页面
					runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							DemoApplication.getInstance().logout(null);
							Toast.makeText(getApplicationContext(), R.string.login_failure_failed, 1).show();
						}
					});
					return;
				}
				// 更新当前用户的nickname 此方法的作用是在ios离线推送时能够显示用户nick
				boolean updatenick = EMChatManager.getInstance().updateCurrentUserNick(
						DemoApplication.currentUserNick.trim());
				if (!updatenick) {
					Log.e("LoginActivity", "update current user nick fail");
				}
				if (!LoginActivity.this.isFinishing() && pd.isShowing()) {
					pd.dismiss();
				}
				// 进入主页面
				Intent intent = new Intent(LoginActivity.this,
						MainActivity.class);
				startActivity(intent);
				
				finish();
			}

			@Override
			public void onProgress(int progress, String status) {
			}

			@Override
			public void onError(final int code, final String message) {
				if (!progressShow) {
					return;
				}
				runOnUiThread(new Runnable() {
					public void run() {
						pd.dismiss();
						Toast.makeText(getApplicationContext(), getString(R.string.Login_failed) + message,
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}



	private void initializeContacts() {
		//SKYMODIFY  不是第一次，直接从本地取
		if (!SkyUserManager.getInstances().isFirstRun(this)){
			SkyUserManager.getInstances().getUserFromDB(this);
			return ;
		}
		Map<String, User> userlist = new HashMap<String, User>();
		//SKYMODIFY  以前 demo中简单的处理成每次登陆都去获取好友username，开发者自己根据情况而定
		// 现在还用以前的设定
		List<String> usernames = new ArrayList<String>();
		try {
			usernames = EMContactManager.getInstance().getContactUserNames();
		} catch (EaseMobException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d("roster", "contacts size: " + usernames.size());
		for (String username : usernames) {
			User user = new User();
			//nickName 缺省也是用的是 username
			user.setUsername(username);
			userlist.put(username, user);
		}
		
		// 添加user"申请与通知"
		User newFriends = new User();
		newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
		String strChat = getResources().getString(
				R.string.Application_and_notify);
		newFriends.setNick(strChat);

		userlist.put(Constant.NEW_FRIENDS_USERNAME, newFriends);
		// 添加"群聊"
		User groupUser = new User();
		String strGroup = getResources().getString(R.string.group_chat);
		groupUser.setUsername(Constant.GROUP_USERNAME);
		groupUser.setNick(strGroup);
		groupUser.setHeader("");
		userlist.put(Constant.GROUP_USERNAME, groupUser);
		
		// 添加"Robot"
		//SKYMODIFY not use robot 
//		User robotUser = new User();
//		String strRobot = getResources().getString(R.string.robot_chat);
//		robotUser.setUsername(Constant.CHAT_ROBOT);
//		robotUser.setNick(strRobot);
//		robotUser.setHeader("");
//		userlist.put(Constant.CHAT_ROBOT, robotUser);
		
		// 存入内存
		DemoApplication.getInstance().setContactList(userlist);
		// 存入db
		UserDao dao = new UserDao(LoginActivity.this);
		List<User> users = new ArrayList<User>(userlist.values());
		dao.saveContactList(users);
		//SKYMODIFY
		SkyUserManager.getInstances().fisrtGetInfo(this,userlist);

	}
	
	/**
	 * 注册
	 * 
	 * @param view
	 */
	public void register(View view) {
	    SkyUtil.showDialog(this, "目前CI功能还没有完成，尚不能注册。科天的每个员工已经预注册了用户。\n用户名，邮箱@前的字符串，比如张军，jun.z，密码Pass1234");
		//startActivityForResult(new Intent(this, RegisterActivity.class), 0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (autoLogin) {
			return;
		}
	}
}
