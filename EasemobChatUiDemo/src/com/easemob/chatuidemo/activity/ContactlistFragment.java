package com.easemob.chatuidemo.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.HideReturnsTransformationMethod;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.easemob.chat.EMChatDB;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactManager;
import com.easemob.chatuidemo.Constant;
import com.easemob.chatuidemo.DemoApplication;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.adapter.ContactAdapter;
import com.easemob.chatuidemo.db.UserDao;
import com.easemob.chatuidemo.domain.User;
import com.easemob.exceptions.EaseMobException;

/**
 * 联系人列表页
 *
 */
public class ContactlistFragment extends Fragment{
	private ContactAdapter adapter;
	private List<User> contactList;
	private ListView listView;
	private boolean hidden;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_contact_list, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		listView = (ListView) getView().findViewById(R.id.list);
		contactList = new ArrayList<User>();
		//获取设置contactlist
		getContactList();
		
		//设置adapter
		adapter = new ContactAdapter(getActivity(), 1, contactList);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String username = adapter.getItem(position).getUsername();
				if(Constant.NEW_FRIENDS_USERNAME.equals(username)){
					//进入新的朋友页面
					User user = DemoApplication.getInstance().getContactList().get(Constant.NEW_FRIENDS_USERNAME);
					user.setUnreadMsgCount(0);
					startActivity(new Intent(getActivity(), NewFriendsMsgActivity.class));
				}else{
					//demo中直接进入聊天页面，实际一般是进入用户详情页
					startActivity(new Intent(getActivity(), ChatActivity.class).putExtra("userId", adapter.getItem(position).getUsername()));
				}
			}
		});
		
		ImageView addContactView = (ImageView) getView().findViewById(R.id.iv_new_contact);
		//进入添加好友页
		addContactView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), AddContactActivity.class));
			}
		});
		registerForContextMenu(listView);
		
	}
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		// if(((AdapterContextMenuInfo)menuInfo).position > 0){ m,
		getActivity().getMenuInflater().inflate(R.menu.delete_contact, menu);
		// }
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.delete_contact) {
			User tobeDeleteUser= adapter.getItem(((AdapterContextMenuInfo) item.getMenuInfo()).position);
			//删除此联系人
			deleteContact(tobeDeleteUser);
			return true;
		}
		return super.onContextItemSelected(item);
	}
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		this.hidden = hidden;
		if(!hidden){
			refresh();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(!hidden){
			refresh();
		}
	}
	
	/**
	 * 删除联系人
	 * @param toDeleteUser
	 */
	public void deleteContact(final User tobeDeleteUser){
		final ProgressDialog pd = new ProgressDialog(getActivity());
		pd.setMessage("正在删除...");
		pd.setCanceledOnTouchOutside(false);
		pd.show();
			new Thread(new Runnable() {
				public void run() {
					try {
						EMContactManager.getInstance().deleteContact(tobeDeleteUser.getUsername());
						//删除db和内存中此用户的数据
						UserDao dao = new UserDao(getActivity());
						dao.deleteContact(tobeDeleteUser.getUsername());
						DemoApplication.getInstance().getContactList().remove(tobeDeleteUser.getUsername());
						getActivity().runOnUiThread(new Runnable() {
							public void run() {
								pd.dismiss();
								adapter.remove(tobeDeleteUser);
								adapter.notifyDataSetChanged();
								
							}
						});
					} catch (final EaseMobException e) {
						getActivity().runOnUiThread(new Runnable() {
							public void run() {
								pd.dismiss();
								Toast.makeText(getActivity(), "删除失败: " + e.getMessage(), 1).show();
							}
						});
						
					}
					
				}
			}).start();
			
	}
	
	//刷新ui
	public void refresh(){
		try {
			//可能会在子线程中调到这方法
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					getContactList();
					adapter.notifyDataSetChanged();
					
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void getContactList(){
		contactList.clear();
		Map<String, User> users = DemoApplication.getInstance().getContactList();
		Iterator<Entry<String, User>> iterator = users.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<String, User> entry = iterator.next();
			if(!entry.getKey().equals(Constant.NEW_FRIENDS_USERNAME))
				contactList.add(entry.getValue());
		}
		//排序
		Collections.sort(contactList, new Comparator<User>() {

			@Override
			public int compare(User lhs, User rhs) {
				return lhs.getUsername().compareTo(rhs.getUsername());
			}
		});
		//把"新的朋友"添加到首位
		contactList.add(0,users.get(Constant.NEW_FRIENDS_USERNAME));
	}
}
