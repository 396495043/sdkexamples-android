/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.skytech.chatim.ui;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.easemob.chat.EMContactManager;
import com.easemob.chatuidemo.DemoApplication;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.activity.AlertDialog;
import com.easemob.chatuidemo.activity.BaseActivity;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.skytech.chatim.proxy.RetrofitClient;
import com.skytech.chatim.sky.retrofit.ServerInterface;
import com.skytech.chatim.sky.util.DataUtil;
import com.skytech.chatim.sky.vo.ListUserResponse;
import com.skytech.chatim.sky.vo.QueryUser;

public final class UserListActivity extends BaseActivity {
    private static String TAG = UserListActivity.class.getSimpleName();
    private PullToRefreshListView mPullRefreshListView;
    private UserListAdapter mAdapter;
    private String queryName;
    private int  offset;
    private int limit = 10 ;
    private Map<String, String> queryMap = new HashMap<String, String>() ;
    private ArrayList<QueryUser> dataList;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sky_query_user_list);
        queryName = getIntent().getStringExtra(DataUtil.IntentKey);
        queryMap.put("limit", ""+limit);
        queryMap.put("query", queryName);
        queryMap.put("offset", ""+offset);
        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
        mPullRefreshListView.setMode(Mode.PULL_FROM_END);
       
        mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {


//            @Override
//            public void onPullDownToRefresh(
//                    PullToRefreshBase<ListView> refreshView) {
////                String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
////                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
////
////                // Update the LastUpdatedLabel
////                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
//
//
//                
//            }
//
//            @Override
//            public void onPullUpToRefresh(
//                    PullToRefreshBase<ListView> refreshView) {
//                // Do work to refresh the list here.
//                refershData();
//                
//            }

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                // TODO Auto-generated method stub
                refershData();
            }
        });
        
        mPullRefreshListView.setEmptyView(getLayoutInflater().inflate(R.layout.sky_empty_query_user, null));
        mPullRefreshListView.getRefreshableView().addFooterView(getLayoutInflater().inflate(R.layout.sky_bottom, null));
        // Add an end-of-list listener
        mPullRefreshListView
                .setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

                    @Override
                    public void onLastItemVisible() {
//                        Toast.makeText(UserListActivity.this, "End of List!",
//                                Toast.LENGTH_SHORT).show();
                    }
                });

        ListView actualListView = mPullRefreshListView.getRefreshableView();
        dataList = new ArrayList<QueryUser>();
        mAdapter = new UserListAdapter(this, dataList);
        actualListView.setAdapter(mAdapter);
        refershData();
    }
    
    private void refershData() {
        final ServerInterface serverInterface = RetrofitClient
                .getServerInterface();
        Log.e(TAG, " queryMap " + queryMap);
        serverInterface.listUser(queryMap, new Callback<ListUserResponse>(){
            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, " listUser  error" + error);
            }

            @Override
            public void success(ListUserResponse data, Response arg1) {
                Log.d(TAG, " listUser " + data);
                offset += limit ;
                queryMap.put("offset", ""+offset);
                QueryUser [] queryUsers = data.getResult().getList() ;
                if (queryUsers!=null){
                    for (int i = 0; i < queryUsers.length; i++) {
                        dataList.add(queryUsers[i]);
                    }      
                    mAdapter.notifyDataSetChanged();
                    if (queryUsers.length < 10){
                        mPullRefreshListView.onRefreshComplete();
                        mPullRefreshListView.setMode(Mode.DISABLED);
                        Log.d(TAG, " setMode(Mode.DISABLED " );
                    }
                }else{
                    mPullRefreshListView.onRefreshComplete();
                    mPullRefreshListView.setMode(Mode.DISABLED);
                    Log.d(TAG, " setMode(Mode.DISABLED ");
                }
   
            }});

    }
    


    public static void query(Activity activity, String queryName) {
        Intent intent = new Intent(activity, UserListActivity.class);
        intent.putExtra(DataUtil.IntentKey, queryName);
        activity.startActivity(intent);

    }

    public void addUser(final Activity activity, final String userName) {
        if (DemoApplication.getInstance().getUserName().equals(userName)) {
            String str = activity.getString(R.string.not_add_myself);
            activity.startActivity(new Intent(activity, AlertDialog.class)
                    .putExtra("msg", str));
            return;
        }

        if (DemoApplication.getInstance().getContactList()
                .containsKey(userName)) {
            // 提示已在好友列表中，无需添加
            if (EMContactManager.getInstance().getBlackListUsernames()
                    .contains(userName)) {
                activity.startActivity(new Intent(activity, AlertDialog.class)
                        .putExtra("msg", "此用户已是你好友(被拉黑状态)，从黑名单列表中移出即可"));
                return;
            }
            String strin = activity
                    .getString(R.string.This_user_is_already_your_friend);
            activity.startActivity(new Intent(activity, AlertDialog.class)
                    .putExtra("msg", strin));
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(activity);
        String stri = activity.getResources().getString(
                R.string.Is_sending_a_request);
        progressDialog.setMessage(stri);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        new Thread(new Runnable() { 
            public void run() {

                try {
                    // demo写死了个reason，实际应该让用户手动填入
                    String s = activity.getResources().getString(
                            R.string.Add_a_friend);
                    EMContactManager.getInstance().addContact(userName, s);
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s1 = activity.getResources().getString(
                                    R.string.send_successful);
                            Toast.makeText(activity.getApplicationContext(),
                                    s1, 1).show();
                        }
                    });
                } catch (final Exception e) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s2 = activity.getResources().getString(
                                    R.string.Request_add_buddy_failure);
                            Toast.makeText(activity.getApplicationContext(),
                                    s2 + e.getMessage(), 1).show();
                        }
                    });
                }
            }
        }).start();
    }
}
