package com.skytech.chatim.proxy;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;

import com.easemob.chatuidemo.DemoApplication;
import com.easemob.chatuidemo.activity.AlertDialog;
import com.umeng.analytics.MobclickAgent;

public class SkyUtil {
    public static boolean isOriginal() {
        return false;
    }

	public static void showDialog(Activity activity,
	        String msg) {
	    activity.startActivity(new Intent(activity, AlertDialog.class).putExtra("msg", msg));
	    
	}

	public static Resources getResources() {
		return DemoApplication.applicationContext.getResources();
	}

	public static String getResStr(int id) {
		return getResources().getString(id);
	}

	public static void logUserAndModel(Activity activity, String username) {
		MobclickAgent.onEvent(activity, "userDevice",username+"--"+Build.MODEL);
	}
}
