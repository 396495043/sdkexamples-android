package com.skytech.chatim.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.chatuidemo.DemoApplication;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.activity.BaseActivity;
import com.skytech.chatim.proxy.RetrofitClient;
import com.skytech.chatim.proxy.SkyProductManager;
import com.skytech.chatim.proxy.SkyUserManager;
import com.skytech.chatim.sky.retrofit.ServerInterface;
import com.skytech.chatim.sky.util.AndroidUtil;
import com.skytech.chatim.sky.util.DataUtil;
import com.skytech.chatim.sky.vo.MeetingLinkResponse;
import com.skytech.chatim.sky.vo.SkyUser;
import com.skytech.chatim.sky.vo.SkyUserResponse;
import com.skytech.chatim.sky.vo.StartLinkPara;
import com.skytech.chatim.sky.vo.UploadFile;
import com.squareup.picasso.Picasso;

public class PersonInfoActivity extends BaseActivity implements OnClickListener {
	private static String TAG = PersonInfoActivity.class.getSimpleName();
	private ImageView iv_header;
	private Button saveButton;
	private ProgressDialog progressDialog;
	private static final int cropRequest = 3;
	private static final int cameraRequest = 4;
	private static final int imageRequest = 5;
	private Bitmap head;// 头像Bitmap

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.sky_activity_person);
		SkyUser skyUser = SkyUserManager.getInstances().getSkyUser();
		saveButton = (Button) findViewById(R.id.btn_save);
		iv_header = (ImageView) findViewById(R.id.iv_header);
		
		if (skyUser.getAvatar() != null) {
			Picasso.with(this).load(skyUser.getAvatar())
					.placeholder(R.drawable.default_avatar).into(iv_header);
		}
		Log.d(TAG," skyUser.getAvatar" + skyUser.getAvatar() + "__");
		saveButton.setOnClickListener(this);
        progressDialog = AndroidUtil.getProgressDialog(this, null);
        setEditValue(R.id.et_input_uid, skyUser.getUid());
        setReadOnly(R.id.et_input_uid);
        showData(skyUser);
        progressDialog.show();
        refreshUserInfo();
	}

	private void setReadOnly(int id) {
		EditText editText = (EditText) findViewById(id);
		editText.setKeyListener(null);
		editText.setTextColor(R.color.readonly);
	}

	private void showData(SkyUser skyUser) {
		setEditValue(R.id.et_input_nickname, skyUser.getNickName());
		setEditValue(R.id.et_input_email, skyUser.getEmail());
		setEditValue(R.id.et_input_org, skyUser.getOrg());
		setEditValue(R.id.et_input_group, skyUser.getGroup());
		setEditValue(R.id.et_input_workPhone, skyUser.getWorkPhone());
		setEditValue(R.id.et_input_phone, skyUser.getPhone());
	}

    public void refreshUserInfo() {
        final ServerInterface serverInterface = RetrofitClient
                .getServerInterface();
        String userName = DemoApplication.getInstance().getUserName();
        serverInterface.getSkyUser(userName, new Callback<SkyUserResponse>() {
            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, " getSkyUserByhx  error" + error);
                AndroidUtil.closeDialog(progressDialog);
            }

            @Override
            public void success(SkyUserResponse skyUserResponse, Response arg1) {
                Log.d(TAG, " skyUser  " + skyUserResponse);
                SkyUserManager.getInstances().setSkyUserInfo(PersonInfoActivity.this, skyUserResponse.getResult());
                SkyUser skyUser = SkyUserManager.getInstances().getSkyUser();
                showData(skyUser);
                getStartLink();
            }
        });
    }

	private void setEditValue(int id, String text) {
		EditText editText = (EditText) findViewById(id);
		editText.setText(text);
	}

	public void changeHeader(View view) {

		AlertDialog.Builder builder = new Builder(PersonInfoActivity.this);
		builder.setItems(getResources().getStringArray(R.array.header),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0) // 拍照
						{
							Intent intent2 = new Intent(
									MediaStore.ACTION_IMAGE_CAPTURE);
							intent2.putExtra(MediaStore.EXTRA_OUTPUT,
									Uri.fromFile(getFile()));
							startActivityForResult(intent2, cameraRequest);// 采用ForResult打开
						} else if (which == 1) // 从手机相册选择
						{
							Intent intent1 = new Intent(Intent.ACTION_PICK,
									null);
							intent1.setDataAndType(
									MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
									"image/*");
							startActivityForResult(intent1, imageRequest);

						}
					}
				});
		Dialog dialog = builder.create();
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.show();

	}

	private File getFile() {
		return new File(Environment.getExternalStorageDirectory() + "/head.jpg");
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case imageRequest:
			if (resultCode == RESULT_OK) {
				cropPhoto(data.getData());// 裁剪图片
			}
			break;
		case cameraRequest:
			if (resultCode == RESULT_OK) {
				cropPhoto(Uri.fromFile(getFile()));// 裁剪图片
			}
			break;
		case cropRequest:
			if (data != null) {
				Bundle extras = data.getExtras();
				head = extras.getParcelable("data");
				if (head != null) {
					/**
					 * 上传服务器代码
					 */
					setPicToView(head);// 保存在SD卡中
					iv_header.setImageBitmap(head);// 用ImageView显示出来
				}
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	};

	/**
	 * 调用系统的裁剪
	 * 
	 * @param uri
	 */
	public void cropPhoto(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, cropRequest);
	}

	private void setPicToView(Bitmap mBitmap) {
		FileOutputStream b = null;
		try {
			b = new FileOutputStream(getFile());
			mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				// 关闭流
				b.flush();
				b.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_save:
			saveInfo();
		}

	}

	private void saveInfo() {
		if (checkEmpty(R.id.et_input_email, R.string.input_error_email)) {
			return;
		}
		if (checkEmpty(R.id.et_input_nickname, R.string.input_error_nickname)) {
			return;
		}
		final String username = SkyUserManager.getInstances().getUserName();
		progressDialog = AndroidUtil.getProgressDialog(this, null);
		progressDialog.show();
		if (head != null) {
			uploadFile();
		} else {
			updateUser(username, null);
		}
	}

	private boolean checkEmpty(int id, int msgId) {
		EditText edit = (EditText) findViewById(id);
		String text = edit.getText().toString().trim();
		if (DataUtil.isEmpty(text)) {
			AndroidUtil.showToast(this, msgId);
			return true;
		}
		return false;
	}

	private void uploadFile() {
		final ServerInterface serverInterface = RetrofitClient
				.getServerInterface();
		String mimeType = "image/jpg";
		TypedFile fileToSend = new TypedFile(mimeType, getFile());
		final String username = SkyUserManager.getInstances().getUserName();
		serverInterface.upload(fileToSend, username,
				new Callback<UploadFile>() {
					@Override
					public void failure(RetrofitError error) {
						// RetrofitClient.dealWithError(this,error);
						Log.e(TAG, " upload  error" + error);
						AndroidUtil.showToast(PersonInfoActivity.this,
								R.string.server_error_upload_file);
						AndroidUtil.closeDialog(progressDialog);
					}

					@Override
					public void success(UploadFile uploadFile, Response arg1) {
						Log.e(TAG, " uploadFile  " + uploadFile);
						String avatar = RetrofitClient.API_URL
								+ ServerInterface.BASE + "files/"
								+ uploadFile.getResult().getId();
						updateUser(username, avatar);
					}

				});
	}

	private void updateUser(final String username, final String avatar) {
		SkyUser skyUserRequest = new SkyUser();
		if (avatar != null) {
			skyUserRequest.setAvatar(avatar);
		}
		setSKyUser(skyUserRequest);

		RetrofitClient.getServerInterface().updateUser(username,
				skyUserRequest, new Callback<SkyUserResponse>() {
					@Override
					public void failure(RetrofitError error) {
						Log.e(TAG, " updateUser  error" + error);
						AndroidUtil.showToast(PersonInfoActivity.this,
								R.string.server_error_update_user);
						AndroidUtil.closeDialog(progressDialog);
					}

					@Override
					public void success(SkyUserResponse skyUserRes,
							Response arg1) {
						Log.d(TAG, " updateUser  " + skyUserRes);
						SkyUser skyUser = SkyUserManager.getInstances()
								.getSkyUser();
						if (avatar != null) {
							skyUser.setAvatar(avatar);
						}
						setSKyUser(skyUser);
						AndroidUtil.showToast(PersonInfoActivity.this,
								R.string.update_user_success);
						AndroidUtil.closeDialog(progressDialog);
						PersonInfoActivity.this.finish();
					}
				});
	}

	private void setSKyUser(SkyUser skyUser) {
		skyUser.setEmail(getEditValue(R.id.et_input_email));
		skyUser.setNickName(getEditValue(R.id.et_input_nickname));
		skyUser.setOrg(getEditValue(R.id.et_input_org));
		skyUser.setGroup(getEditValue(R.id.et_input_group));
		skyUser.setWorkPhone(getEditValue(R.id.et_input_workPhone));
		skyUser.setPhone(getEditValue(R.id.et_input_phone));
	}

	private String getEditValue(int id) {
		EditText editText = (EditText) findViewById(id);
		return editText.getText().toString();
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
					AndroidUtil.showToast(PersonInfoActivity.this, R.string.getMeetingInfoError);
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
		    	TextView text =	(TextView) findViewById(R.id.tv_pmr_meeting);
		    	text.setTextIsSelectable(true);
		    	//String html1 =  "<font color=\"#00008b\" >["+pmrShowLink+ "]</font>";  
		    	//String html1 = "<a href=\""+wbxLink +"\">"+pmrShowLink +"</a>" ; 
		    	text.setText(pmrShowLink);
		    	Button button = (Button) findViewById(R.id.btn_pmr_meeting);
		    	setClickAction(PersonInfoActivity.this, button,wbxLink);
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
		String uid = SkyUserManager.getInstances().getSkyUser().getUid();
		StartLinkPara linkPara = new StartLinkPara(uid, true);
		serverInterface.getMeetingStartLink(linkPara, callback);
		
	}
}
