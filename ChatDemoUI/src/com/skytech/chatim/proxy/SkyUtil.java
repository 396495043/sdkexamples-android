package com.skytech.chatim.proxy;

import android.app.Activity;
import android.content.Intent;

import com.easemob.chatuidemo.activity.AlertDialog;

public class SkyUtil {
    public static boolean isOriginal() {
        return false;
    }

	public static void showDialog(Activity activity,
	        String msg) {
	    activity.startActivity(new Intent(activity, AlertDialog.class).putExtra("msg", msg));
	    
	}

}
