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

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chatuidemo.Constant;
import com.easemob.chatuidemo.DemoApplication;
import com.easemob.chatuidemo.DemoHXSDKModel;
import com.easemob.chatuidemo.R;
import com.skytech.chatim.ui.AboutActivity;
import com.skytech.chatim.ui.PersonInfoActivity;
import com.skytech.chatim.ui.SettingActivity;
import com.skytech.chatim.ui.ShopActivity;
import com.skytech.chatim.ui.SignInActivity;

/**
 * 设置界面
 * 
 * @author Administrator
 * 
 */
public class SettingsFragment extends Fragment implements OnClickListener {

	
	/**
	 * 退出按钮
	 */
	private TextView logoutTV;

	public static SettingsFragment  settingsFragment;

	
	DemoHXSDKModel model;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_conversation_settings, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(savedInstanceState != null && savedInstanceState.getBoolean("isConflict", false))
            return;
		settingsFragment = this ;
		logoutTV = (TextView) getView().findViewById(R.id.tv_logout);
		if(!TextUtils.isEmpty(EMChatManager.getInstance().getCurrentUser())){
			logoutTV.setText(getString(R.string.button_logout) + "  (" + EMChatManager.getInstance().getCurrentUser() + ")");
		}
		logoutTV.setOnClickListener(this);
		//SKYMODIFY
		((LinearLayout) getView().findViewById(R.id.about)).setOnClickListener(this);
		((LinearLayout) getView().findViewById(R.id.personInfo)).setOnClickListener(this);	
		((LinearLayout) getView().findViewById(R.id.logout)).setOnClickListener(this);	
		((LinearLayout) getView().findViewById(R.id.setting)).setOnClickListener(this);	
		((LinearLayout) getView().findViewById(R.id.shop)).setOnClickListener(this);	
		((LinearLayout) getView().findViewById(R.id.sign_in)).setOnClickListener(this);	
	}

	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.logout: //退出登陆
			logout();
			break;
		//SKYMODIFY	
	     case R.id.personInfo:
	            startActivity(new Intent(getActivity(), PersonInfoActivity.class));
	         break;
         case R.id.about:
             startActivity(new Intent(getActivity(), AboutActivity.class));	            
	         break;	     
         case R.id.setting:
             startActivity(new Intent(getActivity(), SettingActivity.class));	
             break;	  
         case R.id.sign_in:
             startActivity(new Intent(getActivity(), SignInActivity.class));
             break;	  
         case R.id.shop:
             startActivity(new Intent(getActivity(), ShopActivity.class));
	         break;	   
		default:
			break;
		}
		
	}

	public void logout() {
		final ProgressDialog pd = new ProgressDialog(getActivity());
		String st = getResources().getString(R.string.Are_logged_out);
		pd.setMessage(st);
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		DemoApplication.getInstance().logout(new EMCallBack() {
			
			@Override
			public void onSuccess() {
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						pd.dismiss();
						// 重新显示登陆页面
						((MainActivity) getActivity()).finish();
						startActivity(new Intent(getActivity(), LoginActivity.class));
						
					}
				});
			}
			
			@Override
			public void onProgress(int progress, String status) {
				
			}
			
			@Override
			public void onError(int code, String message) {
				
			}
		});
	}

	
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
        if(((MainActivity)getActivity()).isConflict){
        	outState.putBoolean("isConflict", true);
        }else if(((MainActivity)getActivity()).getCurrentAccountRemoved()){
        	outState.putBoolean(Constant.ACCOUNT_REMOVED, true);
        }
    }
}
