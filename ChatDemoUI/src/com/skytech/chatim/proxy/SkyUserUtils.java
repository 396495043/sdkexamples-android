package com.skytech.chatim.proxy;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.chatuidemo.DemoApplication;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.db.UserDao;
import com.easemob.chatuidemo.domain.User;
import com.skytech.chatim.sky.retrofit.ServerInterface;
import com.skytech.chatim.sky.util.DataUtil;
import com.skytech.chatim.sky.vo.SkyUser;
import com.skytech.chatim.sky.vo.SkyUserResponse;
import com.squareup.picasso.Picasso;

public class SkyUserUtils {
    private static String TAG = SkyUserUtils.class.getSimpleName();
    /**
     * 设置用户头像
     * @param username
     * @param textView 
     */
    public static void setUserAvatar(final Context context, final String username,final ImageView imageView, final TextView textView){
        if (SkyUtil.isOriginal()){
            Picasso.with(context).load(R.drawable.default_avatar).placeholder(R.drawable.default_avatar).into(imageView);
            return ;
        }
        if (username.equals(DemoApplication.getInstance().getUserName())){
            SkyUser user = SkyUserManager.getInstances().getSkyUser();
            show(context, username, imageView, textView, user.getAvatar(),user.getNickName());
            return ;
        }
       final User user =  getUser(username); 
       if (user == null){
           Log.e(TAG, " can not get user " + username ) ;
           return ;
       }
        show(context, username, imageView, textView, user.getAvatar(),user.getNick());
    }

    public static User getUser(final String username) {
        return SkyUserManager.getInstances().getUser(username);		
    }
    
    public static String getNickName(final String username) {
        User user = getUser(username);
        return getTitle(user);
    }

    public static void refreshUserInfo(final Context context,
            final String username, final ImageView imageView,
            final TextView textView, final User user) {
        final ServerInterface serverInterface = RetrofitClient
                .getServerInterface();

        serverInterface.getSkyUser(username, new Callback<SkyUserResponse>(){
            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, " getSkyUserByhx  error" + error);
            }

            @Override
            public void success(SkyUserResponse skyUser, Response arg1) {
                Log.d(TAG, " get skyUser " + skyUser);
                setUserInfo(context,username,skyUser.getResult());
                if (imageView !=null){
                    show(context, username, imageView, textView, user.getAvatar(),user.getNick());
                }
            }});
    }

    private static void show(Context context, String username,
            final ImageView imageView, final TextView textView, String avatar ,String nickName) {
        Picasso.with(context).load(avatar).placeholder(R.drawable.default_avatar).into(imageView);
        if (textView!=null){
            if (nickName!=null){
            textView.setText(nickName);
            }else{
                textView.setText(username);
            }
        }
    }

    private static boolean invalidUser(User user) {
        if ( DataUtil.isEmpty( user.getAvatar())){
            return true ;
        }
        return false;
    }

    /**
     * 设置用户头像
     * @param username
     * @param textView 
     */
    public static void setUserAvatar(Context context, String username, ImageView imageView){
        setUserAvatar(context,username,imageView,null);
    }

    public static String getTitle(User user) {
        String title = user.getUsername() ;
        if (!DataUtil.isEmpty(user.getNick()))
        {
            title = user.getNick() ;
        }
        return title;
    }
    
    public static  void setUserInfo(final Context activity,
            final String userName, SkyUser skyUser) {
        User user = getUser(userName);
        copySkyUserValue(user,skyUser);
        UserDao userDao = new UserDao(activity);
        userDao.saveContact(user);
    }

	private static void copySkyUserValue(User user, SkyUser skyUser) {
        user.setAvatar(skyUser.getAvatar());
        user.setNick(skyUser.getNickName());

        UserExtend extend = user.getExtend();
		extend.setEmail(skyUser.getEmail());
		extend.setOrg(skyUser.getOrg());
		extend.setGroup(skyUser.getGroup());
		extend.setWorkPhone(skyUser.getWorkPhone());
		extend.setPhone(skyUser.getPhone());
		
	}


    
}
