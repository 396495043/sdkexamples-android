package com.skytech.chatim.proxy;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;

import com.easemob.chatuidemo.DemoApplication;
import com.easemob.chatuidemo.activity.AlertDialog;

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
}
