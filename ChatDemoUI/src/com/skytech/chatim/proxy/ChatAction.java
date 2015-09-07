package com.skytech.chatim.proxy;

import android.app.Activity;

import com.easemob.chatuidemo.activity.ChatActivity;
import com.skytech.chatim.sky.util.DataUtil;
import com.skytech.chatim.sky.vo.MeetingLinkResponse;
import com.skytech.chatim.sky.vo.MeetingLinkResponse.MeetingLinkResult;
import com.skytech.chatim.sky.xmlapi.WebexAPIConstant;

public class ChatAction {
    private static String TAG = ChatAction.class.getSimpleName();
    private String[] sendTexts;
    private ChatActivity activity ;


    public void setSendTexts(String[] sendTexts) {
        this.sendTexts = sendTexts;
    }

    public ChatAction(Activity activityPara) {
    	activity = (ChatActivity)activityPara ;
	}

	public void action(final Activity activity) {
    	SkyProductManager.startOrjoinMeeting(activity, "" ,null ,this);
    }

    public void sendText(MeetingLinkResult meetingLinkResult) {
    	String meetingKey = meetingLinkResult.getMk();
    	String siteName = meetingLinkResult.getSiteName();
    	String userName = SkyUserManager.getInstances().getSkyUser().getUid();
    	userName = userName.replace("@", "");
    	String link = sendTexts[1];
    	link =link.replace("%meetingKey%", meetingKey);
    	link =link.replace("%siteName%", siteName);
    	link =link.replace("%userName%", userName);
    	sendTexts[1] = link ;
        if (sendTexts != null) {
            for (int i = 0; i < sendTexts.length; i++) {
            	activity.sendText(sendTexts[i]);
            }
        }
		
	}

}
