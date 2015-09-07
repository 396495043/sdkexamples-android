package com.skytech.chatim.proxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.easemob.chatuidemo.DemoApplication;
import com.easemob.chatuidemo.activity.LoginActivity;
import com.easemob.chatuidemo.db.UserDao;
import com.easemob.chatuidemo.domain.User;
import com.skytech.chatim.sky.retrofit.ServerInterface;
import com.skytech.chatim.sky.util.DataUtil;
import com.skytech.chatim.sky.vo.LoginEasemobResponse.LoginResult;
import com.skytech.chatim.sky.vo.RestResponse;
import com.skytech.chatim.sky.vo.SkyUser;
import com.skytech.chatim.sky.vo.SkyUserResponse;

/**
 * HX Demo 和 我们应用的集成关系 。 主要是登录集成，好友关系集成和 头像昵称集成。 用户头像昵称集成 。 所有的头像和昵称 ，用 UserUtil
 * 调用 。 本来只有 头像方法，添加了 setUserName 方法 。 都是通过 userName ，到数据库 中去取nickname 和 头像。
 * LoginActivity 中的 processContactsAndGroups ，登陆后，会重新处理 contact 列表 。保存信息到数据库 。
 * 在这时同时到app 服务器上取 nickname 和 头像 信息 。 注意 ，addContact 的时候，同样要取信息 ，删除的时候就不要了。
 * 还有group 聊天室也一样。
 * 
 * @author jason
 *
 */

// SKYMODIFY
public class SkyUserManager {
	public static final String COLUMN_NAME_EMAIL = "email";
	public static final String COLUMN_NAME_COMMENT = "comment";
	private static String TAG = SkyUserManager.class.getSimpleName();
	private static SkyUserManager instantce = new SkyUserManager();
	
	private SkyUser skyUser;
	private String token;
	
	public static SkyUserManager getInstances() {
		return instantce;
	}

	private SkyUserManager() {
	}

	public String getToken() {
		if (token == null){
			DataUtil.readFromPreferences(getContext(), DataUtil.SKY_TOKEN);
		}
		return token;
	}

	public void setToken(String token) {
		this.token = token;
		DataUtil.writeToGlobalPreferences(getContext(), DataUtil.SKY_TOKEN,
				token);
	}

	private Context getContext() {
		return DemoApplication.getInstance();
	}

	public boolean isAllowHxAutoLogin() {
		return false;
	}

	public void saveAccount(String username, String password) {
		Context context = DemoApplication.getInstance();
		DataUtil.writeToGlobalPreferences(context, DataUtil.SKY_USERNAME,
				username);
		DataUtil.writeToGlobalPreferences(context, DataUtil.SKY_PASSWORD,
				password);
	}

	public String getUserName() {
		Context context = DemoApplication.getInstance();
		return DataUtil.readGlobalPreferences(context, DataUtil.SKY_USERNAME);
	}

	public String getPassword() {
		Context context = DemoApplication.getInstance();
		return DataUtil.readGlobalPreferences(context, DataUtil.SKY_PASSWORD);
	}

	public void setSkyUser(LoginResult result) {
		skyUser = new SkyUser();
		skyUser.setUid(result.getUid());
		String nickName = result.getNickName();
		if (DataUtil.isEmpty(nickName)) {
			nickName = DemoApplication.getInstance().getUserName();
		}
		skyUser.setNickName(nickName);
		skyUser.setAvatar((result.getAvatar()));
		skyUser.setEmail((result.getEmail()));
		DataUtil.writeObject(getContext(), DataUtil.MySKyUser, skyUser);
	}

	public SkyUser getSkyUser() {
		if (skyUser == null){
			skyUser = (SkyUser) DataUtil.readObject(getContext(), DataUtil.MySKyUser);
		}
		return skyUser;
	}

	public boolean isFirstRun(Activity activity) {
		if (DataUtil.isEmpty(DataUtil.readFromPreferences(activity,
				DataUtil.HasRun))) {
			DataUtil.writeToPreferences(activity, DataUtil.HasRun, "HasRun");
			return true;
		}
		return false;
	}

	public void getUserFromDB(Activity activity) {
		UserDao dao = new UserDao(activity);
		Map<String, User> userlist = dao.getContactList();

		// 存入内存
		DemoApplication.getInstance().setContactList(userlist);
	}

	public boolean isFilterUser(User user, String prefixString) {
		prefixString = prefixString.trim().toLowerCase();
		return user.getUsername().toLowerCase().indexOf(prefixString) >= 0
				|| user.getNick().toLowerCase().indexOf(prefixString) >= 0;
	}

	public void refreshUserInfoInBg(final Activity activity,final Collection<User> userList) {
		new Thread() {
			@Override
			public void run() {
				refreshUserInfo(activity,userList);
			}
		}.start();
	}
	
	public void refreshUserInfo(final Activity activity,Collection<User> userList) {
		final ServerInterface serverInterface = RetrofitClient
				.getServerInterface();
		for (Iterator iterator = userList.iterator(); iterator.hasNext();) {
			User user = (User) iterator.next();
			String userName = user.getUsername();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			refreshUserInfo(activity, serverInterface, userName);
		}
//		UserDao dao = new UserDao(activity);
//		ArrayList<User> list = new ArrayList<User>();
//		list.addAll(userList);
//		dao.saveContactList(list);
	}

	private void refreshUserInfo(final Activity activity,
			final ServerInterface serverInterface, final String userName) {
		serverInterface.getSkyUser(userName, new Callback<SkyUserResponse>() {
			@Override
			public void failure(RetrofitError error) {
				Log.e(TAG, " refreshUserInfo  error" + error);
			}

			@Override
			public void success(SkyUserResponse skyUserResponse, Response arg1) {
				Log.d(TAG, " skyUser  " + skyUserResponse);
				SkyUserUtils.setUserInfo(activity, userName,
						skyUserResponse.getResult());
			}
		});
	}

	public User getUser(String userName) {
		if (userName.equals(DemoApplication.getInstance().getUserName())) {
			return getMyUser();
		}
		User user = DemoApplication.getInstance().getContactList()
				.get(userName);
		if (user == null) {
			Log.e(TAG, userName
					+ " not find user in contact list ,create new user ");
			user = getNewUser(userName);
		}
		return user;
	}

	private User getMyUser() {
		User user = new User(skyUser.getUid());
		user.setNick(skyUser.getNickName());
		user.setAvatar(skyUser.getAvatar());
		return user;
	}

	private User getNewUser(String userName) {
		return new User(userName);
	}

	public void getExtendValue(Cursor cursor, User user) {
		UserExtend extend = user.getExtend();
		extend.setEmail(cursor.getString(cursor
				.getColumnIndex(COLUMN_NAME_EMAIL)));
		extend.setComment(cursor.getString(cursor
				.getColumnIndex(COLUMN_NAME_COMMENT)));

	}

	public void setExtendValue(User user, ContentValues values) {
		UserExtend extend = user.getExtend();
		if (extend.getEmail() != null)
			values.put(COLUMN_NAME_EMAIL, extend.getEmail());
		if (extend.getComment() != null)
			values.put(COLUMN_NAME_COMMENT, extend.getComment());
	}

	public void setSkyUserInfo(final Context activity, SkyUser result) {
		skyUser.setOrg(result.getOrg());
		skyUser.setGroup(result.getGroup());
		skyUser.setWorkPhone(result.getWorkPhone());
		skyUser.setPhone(result.getPhone());
	}

	public void onContactAdd(Activity activity, String username) {
		// SKYMODIFY ,when Contact change ,need notify sky server .
		final ServerInterface serverInterface = RetrofitClient
				.getServerInterface();
		String myName = SkyUserManager.getInstances().getUserName();
		// add contact at IM app server
		serverInterface.addFriend(myName, username, "",
				new Callback<RestResponse>() {

					@Override
					public void failure(RetrofitError error) {
						Log.e(TAG, " onContactAdded  error" + error);
						// AndroidUtil.showToast(MainActivity.this,
						// R.string.server_error_add_contact);
					}

					@Override
					public void success(RestResponse arg0, Response arg1) {
						Log.d(TAG, " onContactAdded success " + arg0);
						// contactListFragment.refresh();
						// AndroidUtil.showToast(MainActivity.this, " 成功 " );
					}
				});

	}

	public void onContactDeleted(Activity activity, String username) {
		final ServerInterface serverInterface = RetrofitClient
				.getServerInterface();
		String myName = SkyUserManager.getInstances().getUserName();
		// remove contact at IM app server
		serverInterface.deleteFriend(myName, username,
				new Callback<RestResponse>() {

					@Override
					public void failure(RetrofitError error) {
						Log.e(TAG, " onContactRemoved  error" + error);
						// AndroidUtil.showToast(MainActivity.this,
						// R.string.server_error_remove_contact);
					}

					@Override
					public void success(RestResponse arg0, Response arg1) {
						Log.d(TAG, " onContactRemoved  success " + arg0);
						// contactListFragment.refresh();
						// AndroidUtil.showToast(MainActivity.this, " 成功 " );

					}
				});

	}

}
