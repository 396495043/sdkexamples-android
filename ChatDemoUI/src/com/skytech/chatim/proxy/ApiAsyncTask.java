package com.skytech.chatim.proxy;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.skytech.chatim.sky.util.AndroidUtil;
import com.skytech.chatim.sky.util.DataUtil;
import com.skytech.chatim.sky.xmlapi.WebexAPIConstant;
import com.skytech.chatim.sky.xmlapi.WebexConfig;
import com.skytech.chatim.sky.xmlapi.XmlApiManager;
import com.skytech.chatim.ui.BindWebexActivity;


/**
 * Created by jason on 15/3/25.
 */
public class ApiAsyncTask extends AsyncTask<String, Integer, Integer> {
    private BindWebexActivity bindWebexActivity ;
    private WebexConfig webexConfig ;
    private static final String TAG = ApiAsyncTask.class.getSimpleName();
    private ProgressDialog progressDialog;
    public ApiAsyncTask(BindWebexActivity bindWebexActivity,WebexConfig webexConfig){
        this.bindWebexActivity = bindWebexActivity ;
        this.webexConfig = webexConfig ;
    }

    @Override
    protected Integer doInBackground(String... strings) {
        return invokeAPI();
    }
    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        AndroidUtil.closeDialog(progressDialog);
        bindWebexActivity.result(result);
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = AndroidUtil.getProgressDialog(bindWebexActivity, null);
        progressDialog.show();
    }

    // 登录
    private Integer invokeAPI() {
       try {
            String key =  XmlApiManager.getInstance().getUserMeetingKey(webexConfig);
            if (Long.parseLong(key) <=0){
                return  -1 ;
            }
            DataUtil.writeToPreferences(bindWebexActivity, WebexAPIConstant.WBX_PMR_MEETING_KEY, key);
            Log.d(TAG, " czqresult " + key);
            return 1 ;
        } catch (Exception e) {
            e.printStackTrace();
            return -1 ;
        }

    }


}
