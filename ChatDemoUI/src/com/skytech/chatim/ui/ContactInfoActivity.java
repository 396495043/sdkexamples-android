package com.skytech.chatim.ui;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.activity.BaseActivity;
import com.easemob.chatuidemo.domain.User;
import com.skytech.chatim.proxy.RetrofitClient;
import com.skytech.chatim.proxy.SkyProductManager;
import com.skytech.chatim.proxy.SkyUserManager;
import com.skytech.chatim.proxy.SkyUserUtils;
import com.skytech.chatim.proxy.SkyUtil;
import com.skytech.chatim.proxy.UserExtend;
import com.skytech.chatim.sky.retrofit.ServerInterface;
import com.skytech.chatim.sky.util.AndroidUtil;
import com.skytech.chatim.sky.util.DataUtil;
import com.skytech.chatim.sky.vo.MeetingLinkResponse;
import com.skytech.chatim.sky.vo.SkyUserResponse;
import com.skytech.chatim.sky.vo.StartLinkPara;
import com.squareup.picasso.Picasso;

public class ContactInfoActivity extends BaseActivity {
    private static String TAG = ContactInfoActivity.class.getSimpleName();
    private ImageView iv_header;
    private ProgressDialog progressDialog;
    private String userName;
	private User user;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        userName = getIntent().getStringExtra(DataUtil.IntentKey);
        setContentView(R.layout.sky_activity_contact);
        ((TextView) findViewById(R.id.tv_username))
                .setText(userName);
        progressDialog = AndroidUtil.getProgressDialog(this, null);
        user = SkyUserManager.getInstances().getUser(userName);
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
    	text.setTextIsSelectable(true);
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
                user = SkyUserManager.getInstances().getUser(userName);
                showData(user);
                //getStartLink();
                AndroidUtil.closeDialog(progressDialog);
                setShareAction();
            }
        });
    }

	private void getStartLink() {
        final ServerInterface serverInterface = RetrofitClient
                .getServerInterface();
        Callback<MeetingLinkResponse> callback = new Callback<MeetingLinkResponse>() {
		    @Override
		    public void failure(RetrofitError error) {
		        try {
					Log.e(TAG, " getMeetingLink  error" + error);
					AndroidUtil.closeDialog(progressDialog);
					AndroidUtil.showToast(ContactInfoActivity.this, R.string.getMeetingInfoError);
				} catch (Exception e) {
					// 保护 ，实际上不应该 有exception 在这里 catch 住。
					Log.e(TAG, " Callback<MeetingLinkResponse> failure ",e);
				}
		    }

		    @Override
		    public void success(MeetingLinkResponse response,
		            Response arg1) {
		        Log.d(TAG, " get MeetingLinkResponse " + response);
		        AndroidUtil.closeDialog(progressDialog);
		        Intent intent = new Intent(Intent.ACTION_VIEW);
		        intent.setData(Uri.parse(response.getResult().getUrl()));
		    	String pmrShowLink = response.getResult().getPersonalMeetingUrl();
		    	String wbxLink = response.getResult().getUrl() ;
		    	wbxLink = getJoinLink(wbxLink);
		    	TextView text =	(TextView) findViewById(R.id.tv_pmr_meeting);
		    	text.setTextIsSelectable(true);
		    	text.setText(pmrShowLink);
		    	Button button = (Button) findViewById(R.id.btn_pmr_meeting);
		    	setClickAction(ContactInfoActivity.this, button,wbxLink);
		    	setShareAction();
		    	AndroidUtil.closeDialog(progressDialog);
		    }

			private void setClickAction(final Activity activity ,final Button button, final String wbxLink) {
				button.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
				        intent.setData(Uri.parse(wbxLink));
				        activity.startActivityForResult(intent,SkyProductManager.WebexInvokeRequest);
					}
				});
			}
		};
		StartLinkPara linkPara = new StartLinkPara(user.getUsername(), true);
		serverInterface.getMeetingStartLink(linkPara, callback);
		
	}
	
	private String getJoinLink(String wbxLink) {
		int start = wbxLink.indexOf("&TK=");
		return wbxLink.substring(0,start);
	}
	
	private void setShareAction() {
		Button button = (Button)findViewById(R.id.btn_share);
		button.setOnClickListener(new View.OnClickListener() {				
			@Override
			public void onClick(View v) {
		        share();
			}
		});
		
	}
	
	private void share() {
		//http://blog.csdn.net/loongggdroid/article/details/35572269
		Intent intent = new Intent(Intent.ACTION_SEND);  
		//分享文字
		intent.setType("text/plain"); // 纯文本 
		String text = getShareText();
        intent.putExtra(Intent.EXTRA_SUBJECT, "");  
        intent.putExtra(Intent.EXTRA_TEXT, text);  
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
        startActivity(Intent.createChooser(intent, ""));  
	}
	
	
	private String getShareText() {
		StringBuffer sb = new StringBuffer();
		sb.append(getShareTextString(R.string.nickName,R.id.tv_nickname));
		sb.append(getShareTextString(R.string.email,R.id.tv_email));
		sb.append(getShareTextString(R.string.org,R.id.tv_org));
		sb.append(getShareTextString(R.string.group,R.id.tv_group));
		sb.append(getShareTextString(R.string.phone,R.id.tv_phone));
		sb.append(getShareTextString(R.string.workPhone,R.id.tv_workPhone));
		sb.setLength(sb.length()-2);
		return sb.toString();
	}


	private String getShareTextString(int sid, int inputID) {
		return getShareText(sid,inputID) + "\r\n";
	}
	private String getShareText(int sid, int inputID) {
		return SkyUtil.getFixWithString(getString(sid),4)+":" + getEditValue(inputID);
	}

	private String getEditValue(int id) {
		TextView text =	(TextView) findViewById(id);
		return text.getText().toString();
	}
}
