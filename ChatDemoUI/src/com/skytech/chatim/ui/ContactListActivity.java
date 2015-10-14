package com.skytech.chatim.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.activity.BaseActivity;
import com.easemob.chatuidemo.activity.ChatActivity;
import com.easemob.chatuidemo.activity.GroupsActivity;
import com.skytech.chatim.proxy.HxManager;
import com.skytech.chatim.sky.util.DataUtil;
import com.skytech.chatim.sky.vo.ContactUser;

public final class ContactListActivity extends BaseActivity {
	public static final String GROUP_ID = "groupId";
	public static final String USER_ID = "userId";
	private static String TAG = ContactListActivity.class.getSimpleName();
	private ListView listView;
	private ContactListAdapter mAdapter;
	//private String type;
	private ArrayList<ContactUser> dataList;
	private static final String SHARE_LIST = "SHARE_LIST";
	private static final String AT_LIST = "AT_LIST";
	private int type = SHARE_TYPE  ;
	private static final  int AT_TYPE  = 1;
	private static final  int SHARE_TYPE  = 0;
	private static final String GROUP_ID_KEY ="GROUP_ID_KEY" ;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent it = getIntent();
		if (it != null && it.getAction() != null
				&& it.getAction().equals(Intent.ACTION_SEND)) {
			Bundle extras = it.getExtras();
			dataList = HxManager.getInstances().getContactList();

		}
		String groupID = getIntent().getStringExtra(GROUP_ID_KEY);
		if (!DataUtil.isEmpty(groupID)){
			  EMGroup group = EMGroupManager.getInstance().getGroup(groupID);
			  dataList = HxManager.getInstances().getGroupList(group);
			  type = AT_TYPE ;
		}
		
		setContentView(R.layout.sky_contact_list);
		listView = (ListView) findViewById(R.id.list);
		mAdapter = new ContactListAdapter(this, dataList);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				selectUser((ContactUser)mAdapter.getItem(position));
				
			}} );
		listView.addFooterView(getLayoutInflater().inflate(R.layout.sky_bottom, null));
	}

	public void selectUser(ContactUser contactUser) {
		if (type == SHARE_TYPE ){
			Intent intent = new Intent(this, ChatActivity.class);
			intent.putExtras(getIntent());
			if (contactUser.getType() == ContactUser.CONTACT_TYPE) {
				intent.putExtra(USER_ID, contactUser.getUid());
				startActivityForResult(intent, 0);
			} else {
				// 进入群聊
				// it is group chat
				intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
				intent.putExtra(GROUP_ID, contactUser.getUid());
				startActivityForResult(intent, 0);
			}
		}else{	
			Intent intent = new Intent();
			intent.putExtra(USER_ID, contactUser.getUid());
			setResult(RESULT_OK,intent);
			Log.d(TAG," USER_ID= " +contactUser.getUid());
		}
		finish();
	}

	public static void showGroupAtList(TextWatcher textWatcher,
			int requestCodeSkyGroupAtList) {
		// TODO Auto-generated method stub
		
	}

	public static void showGroupAtList(Activity activity,
			int requestCode, String groupID) {
		Intent intent = new Intent(activity,ContactListActivity.class);
		intent.putExtra(GROUP_ID_KEY, groupID);
		activity.startActivityForResult(intent,requestCode);	
	}
}
