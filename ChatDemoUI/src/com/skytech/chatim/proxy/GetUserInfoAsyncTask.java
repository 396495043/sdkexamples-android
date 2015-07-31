package com.skytech.chatim.proxy;


import android.content.Context;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.db.UserDao;
import com.easemob.chatuidemo.domain.User;
import com.squareup.picasso.Picasso;


/**
 * Created by jason on 15/6/25.
 */
public class GetUserInfoAsyncTask extends AsyncTask<String, Integer, Integer> {

    private static final String TAG = GetUserInfoAsyncTask.class.getSimpleName();
    private Context context ;
    private String userName ;
    private  ImageView imageView ;
    private  TextView textView;
    private String avatar ;
    private String nickName ;

    public GetUserInfoAsyncTask(Context context, String userName,
            ImageView imageView, TextView textView) {
        this.context = context ;
        this.userName = userName ;
        this.imageView = imageView ;
        this.textView = textView ;

    }

    @Override
    protected Integer doInBackground(String... strings) {
        return getUserInfo();
    }
    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        User user = new User(userName);
        user.setNick(nickName);
        user.setAvatar(avatar);
        UserDao userDao = new UserDao(context);
        userDao.saveContact(user);
        Picasso.with(context).load(avatar).placeholder(R.drawable.default_avatar).into(imageView);
        textView.setText(nickName);
        
    }
    // 登录
    private Integer getUserInfo() {

        return 0 ;

    }


}
