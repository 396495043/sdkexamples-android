package com.skytech.chatim.proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chatuidemo.Constant;
import com.easemob.chatuidemo.DemoApplication;
import com.easemob.chatuidemo.db.UserDao;
import com.easemob.chatuidemo.domain.User;
import com.skytech.chatim.sky.retrofit.ServerInterface;
import com.skytech.chatim.sky.util.DataUtil;
import com.skytech.chatim.sky.vo.ContactUser;
import com.skytech.chatim.sky.vo.LoginEasemobResponse.LoginResult;
import com.skytech.chatim.sky.vo.RestResponse;
import com.skytech.chatim.sky.vo.SkyUser;
import com.skytech.chatim.sky.vo.SkyUserResponse;


public class HxManager {

	private static String TAG = HxManager.class.getSimpleName();
	private static HxManager instantce = new HxManager();
	public static HxManager getInstances() {
		return instantce;
	}

	private HxManager() {
	}

	public ArrayList<ContactUser> getContactList() {
		   List<String> blackList = EMContactManager.getInstance().getBlackListUsernames();
			ArrayList<ContactUser> contactList = new ArrayList<ContactUser> ();
			//获取本地好友列表
			Map<String, User> users = DemoApplication.getInstance().getContactList();
			Iterator<Entry<String, User>> iterator = users.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, User> entry = iterator.next();
				if (!entry.getKey().equals(Constant.NEW_FRIENDS_USERNAME)
				        && !entry.getKey().equals(Constant.GROUP_USERNAME)
				        && !entry.getKey().equals(Constant.CHAT_ROOM)
						&& !entry.getKey().equals(Constant.CHAT_ROBOT)
						&& !blackList.contains(entry.getKey()))
					contactList.add(getContactUser(entry.getValue() ));
			}
			
			List<EMGroup> grouplist = EMGroupManager.getInstance().getAllGroups();
			for (Iterator iterator2 = grouplist.iterator(); iterator2.hasNext();) {
				EMGroup emGroup = (EMGroup) iterator2.next();
				contactList.add(getContactUser(emGroup ));
			}
			sort(contactList);
			return contactList ;


	}

	private void sort(ArrayList<ContactUser> contactList) {
		// 排序
		Collections.sort(contactList, new Comparator<ContactUser>() {

			@Override
			public int compare(ContactUser lhs, ContactUser rhs) {
			if (lhs.getType() == rhs.getType()) {
				return lhs.getNickName().compareTo(rhs.getNickName());
			} else {
				return lhs.getType() - rhs.getType();
			}
			}
		});
	}

	private ContactUser getContactUser(EMGroup emGroup) {
		return new ContactUser(emGroup.getName(),emGroup.getName(),null,ContactUser.GROUP_TYPE);
	}

	private ContactUser getContactUser(User user  ) {
		return new ContactUser(user.getNick(),user.getUsername(),user.getAvatar(),ContactUser.CONTACT_TYPE);
	}



	public ArrayList<ContactUser> getGroupUserList(EMGroup group) {
		List<String> list = group.getMembers();
		ArrayList<ContactUser> contactList = new ArrayList<ContactUser>();
		Iterator<String> iterator = list.iterator();
		while (iterator.hasNext()) {
			String userName = iterator.next();
			if (!userName.equals(SkyUserManager.getInstances().getUserName())){
				User user = SkyUserUtils.getUser(userName);
				contactList.add(getContactUser(user));
			}
		}
		sort(contactList);
		return contactList;
	}

}
