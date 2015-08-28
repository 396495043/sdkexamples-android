package com.skytech.chatim.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.activity.BaseActivity;
import com.easemob.chatuidemo.activity.ChatActivity;
import com.easemob.chatuidemo.activity.GroupsActivity;
import com.skytech.chatim.proxy.HxManager;
import com.skytech.chatim.sky.util.DataUtil;
import com.skytech.chatim.sky.vo.ContactUser;

public final class ContactListActivity extends BaseActivity {
	private static String TAG = ContactListActivity.class.getSimpleName();
	private ListView listView;
	private ContactListAdapter mAdapter;
	private String type;
	private ArrayList<ContactUser> dataList;
	private static final String SHARE_LIST = "SHARE_LIST";
	private static final String AT_LIST = "AT_LIST";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent it = getIntent();
		if (it != null && it.getAction() != null
				&& it.getAction().equals(Intent.ACTION_SEND)) {
			Bundle extras = it.getExtras();
			dataList = HxManager.getInstances().getContactList();
//			Uri uri = null ;
//			if (extras.containsKey("android.intent.extra.STREAM")) {
//				uri = (Uri) extras.get("android.intent.extra.STREAM");
//				// set_image(uri);//这里是将我们所选的分享图片加载出来
//			}
//			String subject = it.getStringExtra(Intent.EXTRA_SUBJECT);
//			String text = it.getStringExtra(Intent.EXTRA_TEXT);
//			Log.d(TAG, " data " + "subject " + subject + "text " + text + "uri " + uri);
			setContentView(R.layout.sky_contact_list);
			type = getIntent().getStringExtra(DataUtil.IntentKey);
			listView = (ListView) findViewById(R.id.list);
			mAdapter = new ContactListAdapter(this, dataList);
			listView.setAdapter(mAdapter);
			listView.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					selectUser((ContactUser)mAdapter.getItem(position));
					
				}} );}
	}

	public void selectUser(ContactUser contactUser) {
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtras(getIntent());
		if (contactUser.getType() == ContactUser.CONTACT_TYPE) {
			// demo中直接进入聊天页面，实际一般是进入用户详情页
			intent.putExtra("userId", contactUser.getUid());
			startActivityForResult(intent, 0);
		} else {
			// 进入群聊
			// it is group chat
			intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
			intent.putExtra("groupId", contactUser.getUid());
			startActivityForResult(intent, 0);
		}
		finish();
	}
}
