package com.skytech.chatim.proxy;

import android.app.Activity;

public class DebugTool {

	public static String getDebugVersion(Activity activity) {
		if ("com.skytech.chatim".equals(getPackageName(activity))){
			return "" ;
		}else{
			return " debug" ;
		}
	}

	private static String getPackageName(Activity activity) {
		return activity.getPackageName();
//		final PackageManager pm = activity.getApplicationContext().getPackageManager();
//		pm.getA
//		ApplicationInfo ai;
//		try {
//		    ai = pm.getApplicationInfo( activity.getPackageName(), 0);
//		} catch (final NameNotFoundException e) {
//		    ai = null;
//		}
	}

	
}
