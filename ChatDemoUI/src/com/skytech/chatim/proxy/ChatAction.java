package com.skytech.chatim.proxy;

import android.app.Activity;

public class ChatAction {
    private static String TAG = ChatAction.class.getSimpleName();
    private String[] sendTexts;

    public String[] getSendTexts() {
        return sendTexts;
    }

    public void setSendTexts(String[] sendTexts) {
        this.sendTexts = sendTexts;
    }

    public void action(final Activity activity) {
        SkyProductManager.startOrjoinMeeting(activity, "");
    }

}
