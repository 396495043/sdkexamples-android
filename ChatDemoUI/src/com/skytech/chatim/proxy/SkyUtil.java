package com.skytech.chatim.proxy;

import java.util.HashMap;

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
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("value",username+"--"+Build.MODEL);
		MobclickAgent.onEvent(activity, "userDevice",map);
	}

	public static String getDevice() {
		return "A;"+ Build.MODEL+";"+Build.VERSION.SDK_INT;
	}
}
