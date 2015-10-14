package com.skytech.chatim.proxy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chatuidemo.R;
import com.easemob.exceptions.EaseMobException;
import com.skytech.chatim.sky.retrofit.ServerInterface;
import com.skytech.chatim.sky.util.AndroidUtil;
import com.skytech.chatim.sky.util.DataUtil;
import com.skytech.chatim.sky.vo.JoinLinkPara;
import com.skytech.chatim.sky.vo.MeetingLinkResponse;
import com.skytech.chatim.sky.vo.StartLinkPara;
import com.skytech.chatim.sky.xmlapi.WebexAPIConstant;


/**
 * 
 * @author jason
 *
 */

public class SkyProductManager {
    static final String SKYMK = "?SKYMK=";
	public static final String AT_USER_LIST ="AT_USER_LIST" ;
	public static final String AT_FROM ="AT_FROM" ;
    private static String TAG = SkyProductManager.class.getSimpleName();
    private static SkyProductManager instantce = new SkyProductManager() ;

    public static SkyProductManager getInstances() {
        return instantce;
    }

    private  SkyProductManager() {
    }


    public  ChatAction  getChatAction(Activity activity ,String from, String to) {
        String text = from + activity.getString(R.string.webexInvite);
        String link =  ""; 
        String [] sendTexts = { text ,link } ;
        ChatAction chatAction = new ChatAction(activity);
        chatAction.setSendTexts(sendTexts);
        return chatAction ;
    }





    public void managerSpan(TextView tv) {
        CharSequence text = tv.getText();   
        if(text instanceof Spannable){   
            int end = text.length();   
            Spannable sp = (Spannable)tv.getText();   
            URLSpan[] urls=sp.getSpans(0, end, URLSpan.class); 
            if (urls.length !=1 ){
            	return ;
            }
            SpannableStringBuilder style=new SpannableStringBuilder(text);   
            style.clearSpans();//should clear old spans   
            for(URLSpan url : urls){   
                ClickableSpan clickableSpan = url ;
                if (isWebexLink(url.getURL())){
                    Log.d(TAG, " isWebexLink " );
                    clickableSpan = new MyURLSpan(url.getURL()); 
                }
                style.setSpan(clickableSpan,sp.getSpanStart(url),sp.getSpanEnd(url),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);   
            }   
            tv.setText(style);   
        }   
        
    }


    private boolean isWebexLink(String url) {
         try {
            URL myURL = new URL(url);
             String host  = myURL.getHost() ;
             String query = myURL.getQuery();
             Log.d(TAG, " host " + host
                     + " query " +query);
             if (host.endsWith("webex.com.cn") && url.contains(SKYMK)){
                 return true ;
             }
        } catch (MalformedURLException e) {
            return false;
        }
        return false;
    }


    private static class MyURLSpan extends ClickableSpan{   
        
        private String mUrl;   
        MyURLSpan(String url) {   
            mUrl =url;   
        }   
        @Override
        public void onClick(View widget) {
                Log.d(TAG, " onClick " + mUrl);
                if (SkyProductManager.getInstances().notSupportWebex((Activity)widget.getContext())){
                    return ;
                }
                String mk = parserMK(mUrl);
                String siteName = parserSite(mUrl);
                startOrjoinMeeting((Activity)widget.getContext(),mk ,siteName,null);
        }


    }

    private static String parserMK(String url) {
        int index = url.indexOf(SKYMK);
        return url.substring(index + SKYMK.length());
    }
    
	private static String parserSite(String url) {
		try {
			URL myURL = new URL(url);
			String host = myURL.getHost().trim();
			int index = host.indexOf(".");
			String siteName = host.substring(0, index);
			return siteName;
		} catch (MalformedURLException e) {
			return null;
		}
	}
    public static void startOrjoinMeeting(final Activity activity, String mk, String siteName ,final ChatAction chatAction) {
        final ServerInterface serverInterface = RetrofitClient
                .getServerInterface();
        if (mk == null){
            mk ="";
        }
        Log.e(TAG, " startOrjoinMeeting mk " + mk
                + " chatAction " + chatAction);
        final ProgressDialog pd = AndroidUtil.getProgressDialog(activity, activity.getString(R.string.getMeetingInfo));
        pd.show();
//		if (mk.endsWith(DataUtil.readFromPreferences(activity,
//				WebexAPIConstant.WBX_PMR_MEETING_KEY))) {
//			// 自己的personnal meeting ，不用join ，还是start
//			mk = "";
//		}
        Callback<MeetingLinkResponse> callback = new Callback<MeetingLinkResponse>() {
		    @Override
		    public void failure(RetrofitError error) {
		        Log.e(TAG, " getMeetingLink  error" + error
		                + " getBody " + error.getBody());
		        AndroidUtil.closeDialog(pd);
		        AndroidUtil.showToast(activity, R.string.getMeetingInfoError);
		    }

		    @Override
		    public void success(MeetingLinkResponse response,
		            Response arg1) {
		        Log.d(TAG, " get MeetingLinkResponse " + response);
		        AndroidUtil.closeDialog(pd);
		        Intent intent = new Intent(Intent.ACTION_VIEW);
		        intent.setData(Uri.parse(response.getResult().getUrl()));
		        activity.startActivity(intent);
		        if (chatAction!=null){
		        	chatAction.sendText(response.getResult());
		        }
		    }
		};
		String uid = SkyUserManager.getInstances().getSkyUser().getUid();
		if (DataUtil.isEmpty(mk)) { // start link
			StartLinkPara linkPara = new StartLinkPara(uid, true);
			serverInterface.getMeetingStartLink(linkPara, callback);
		} else {
			JoinLinkPara linkPara = new JoinLinkPara(siteName, mk);
			serverInterface.getMeetingJoinLink(linkPara, callback);
		}
    }


    public boolean notSupportWebex(Activity activity) {
        String MK= DataUtil.readFromPreferences(activity, WebexAPIConstant.WBX_PMR_MEETING_KEY);
        if (AndroidUtil.notInstallWebex(activity)){
            //AndroidUtil.showLongToast(this, getString(R.string.needInstallWebex));
            //AndroidUtil.startLink(this,SkyProductManager.getInstances().getPMRLink(this));
            DownloadUtil.downloadWebex(activity);
            return true  ;
        }
//        if (DataUtil.isEmpty(MK)){
//            AndroidUtil.showLongToast(activity, activity.getString(R.string.needBindWebex));
//            activity.startActivity(new Intent(activity, BindWebexActivity.class));     
//            return true ;
//        }
        return false ;
        
    }

	public void checkAtMessage(Set<String> atUserSet, EMMessage message, String content) {
		if (atUserSet!=null && atUserSet.size() > 0){
			Log.d(TAG,atUserSet.toString());
			String userNameList ="" ;
			for (Iterator<String> iterator = atUserSet.iterator(); iterator.hasNext();) {
				String name = (String) iterator.next();
				String nickName = SkyUserUtils.getNickName(name);
				if (content.indexOf("@"+nickName) >= 0){
					userNameList += name +",";
				}
			} 
			userNameList = userNameList.substring(0,userNameList.length()-1);
			message.setAttribute(AT_USER_LIST, userNameList);
			message.setAttribute(AT_FROM, SkyUserManager.getInstances().getUserName());
			atUserSet.clear();
		}
	}

	public void showAtMessageAtNotify(Builder mBuilder, EMMessage message, String contentTitle, String ticker, String contentText) {
		try {
		String atMessage =	getAtMessage(message);
		if (atMessage!=null){
			mBuilder.setContentTitle(atMessage + " " +  contentTitle);
			mBuilder.setTicker(atMessage + " " +  ticker);
			mBuilder.setContentText("["+atMessage + "]" +  contentText);
		}
		} catch (EaseMobException e) {
			Log.e(TAG, "showAtMessageAtNotify ",e);
		}
		
	}

	private String getAtMessage(EMMessage message) throws EaseMobException {
		String atFrom;
		try {
			atFrom = message.getStringAttribute(AT_FROM);
		} catch (Exception e) {
				return null ;
		}
		if (atFrom != null){
			String userNameList = message.getStringAttribute(AT_USER_LIST);
			String [] userArray = userNameList.split(",");
			for (int i = 0; i < userArray.length; i++) {
				if (SkyUserManager.getInstances().getUserName().equals(userArray[i])){
					String nickName = SkyUserUtils.getNickName(atFrom);
					String atMessage = nickName +  SkyUtil.getResStr(R.string.atMessage);
					Log.d(TAG," get atMessage " + atMessage);
					return atMessage ;
				}
			}
		}
		return null ;
	}

	public void showAtMessageAtHistory(EMConversation conversation,
			TextView nameTextView) {
		try {
			if (conversation.getIsGroup()){
				int count = conversation.getUnreadMsgCount();
				for (int i = 0; i < count; i++) {
					EMMessage message = conversation.getMessage(i);
					if (message.getType() == EMMessage.Type.TXT){
						String atMessage =	getAtMessage(message);
						if (atMessage != null){
							String html1 =nameTextView.getText() + "<font color=\"#ff0000\" >["+atMessage+ "]</font>";  
							nameTextView.setText(Html.fromHtml(html1));
							break ;
						}
					}
				}
			}
		} catch (EaseMobException e) {
			Log.e(TAG, "showAtMessageAtHistory ",e);
		}
		
	}
    

}
