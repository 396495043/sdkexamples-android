package com.skytech.chatim.proxy;

import java.util.HashMap;

import android.content.Context;
import android.util.Log;

import com.easemob.chat.EMConversation;
import com.easemob.chatuidemo.DemoApplication;
import com.skytech.chatim.sky.util.DataUtil;



public class TopManager {

	private static String TAG = TopManager.class.getSimpleName();
	private static TopManager instantce = new TopManager();
	public static TopManager getInstances() {
		return instantce;
	}
	public HashMap<String,Boolean> mTopMap  = new HashMap<String,Boolean>(); 
	private String  HashSetKey ="HashSet"  ;
	private TopManager() {
	}

	public boolean isTop(EMConversation selectCons) {
		String username = selectCons.getUserName();
		HashMap<String,Boolean> topMap = getTopMap();
		Boolean value = (Boolean)topMap.get(username);
		boolean b = (value == null?false:value.booleanValue());
		//Log.d(TAG,"isTop "+b);
		return b;
	}

	private HashMap<String,Boolean> getTopMap() {
		if (mTopMap == null){
			Context context = DemoApplication.getInstance();
			mTopMap = (HashMap<String,Boolean>)DataUtil.readObject(context,HashSetKey);
			if (mTopMap == null){
				mTopMap = new HashMap<String,Boolean>();
			}
		}
		return mTopMap;
	}
	
	public void setTop(EMConversation selectCons, boolean b) {
		HashMap<String,Boolean> topMap = getTopMap();
		String username = selectCons.getUserName();
		topMap.put(username,new Boolean(b));
		saveMap(topMap);
		Log.d(TAG,"setTop "+ username + " " + b);
	}

	private void saveMap(HashMap<String, Boolean> topMap) {
		mTopMap = topMap ;
		Context context = DemoApplication.getInstance();
		DataUtil.writeObject(context,HashSetKey,topMap);
	}
}
