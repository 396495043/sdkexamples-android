package com.skytech.chatim.proxy;

import java.net.MalformedURLException;
import java.net.URL;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.easemob.chatuidemo.R;
import com.skytech.chatim.sky.retrofit.ServerInterface;
import com.skytech.chatim.sky.util.AndroidUtil;
import com.skytech.chatim.sky.util.DataUtil;
import com.skytech.chatim.sky.vo.MeetingLinkResponse;
import com.skytech.chatim.sky.vo.WebexAccount;
import com.skytech.chatim.sky.xmlapi.WebexAPIConstant;
import com.skytech.chatim.ui.BindWebexActivity;


/**
 * 
 * @author jason
 *
 */

public class SkyProductManager {
    private static final String SKYMK = "?SKYMK=";
    private static String TAG = SkyProductManager.class.getSimpleName();
    private static SkyProductManager instantce = new SkyProductManager() ;

    public static SkyProductManager getInstances() {
        return instantce;
    }

    private  SkyProductManager() {
    }


    public  ChatAction  getSendText(Activity activity ,String from, String to) {
        String text = from + activity.getString(R.string.webexInvite);
        String link =  getPMRLink(activity) ; 
        String [] sendTexts = { text ,link } ;
        ChatAction chatAction = new ChatAction();
        chatAction.setSendTexts(sendTexts);
        return chatAction ;
    }


    public String getPMRLink(Activity activity) {
        String userName = DataUtil.readFromPreferences(activity, WebexAPIConstant.WBX_USERNAME);
        userName = userName.replace("@", "");
        String site =  DataUtil.readFromPreferences(activity, WebexAPIConstant.WBX_SITE);
        return "https://"+site+".webex.com.cn/meet/"+userName+SKYMK+ DataUtil.readFromPreferences(activity, WebexAPIConstant.WBX_PMR_MEETING_KEY);
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
                startOrjoinMeeting((Activity)widget.getContext(),mk);
        }

    }

    private static String parserMK(String url) {
        int index = url.indexOf(SKYMK);
        return url.substring(index + SKYMK.length());
    }
    
    public static void startOrjoinMeeting(final Activity activity, String mk) {
        final ServerInterface serverInterface = RetrofitClient
                .getServerInterface();
        if (mk == null){
            mk ="";
        }
        final ProgressDialog pd = AndroidUtil.getProgressDialog(activity, "");
        pd.show();
        if (mk.endsWith(DataUtil.readFromPreferences(activity,
                        WebexAPIConstant.WBX_PMR_MEETING_KEY))){
                // 自己的personnal meeting ，不用join ，还是start 
                            mk ="" ;
                        }
        WebexAccount webexAccount = new WebexAccount(
                DataUtil.readFromPreferences(activity,
                        WebexAPIConstant.WBX_USERNAME),
                DataUtil.readFromPreferences(activity,
                        WebexAPIConstant.WBX_PASSWORD),
                DataUtil.readFromPreferences(activity,
                        WebexAPIConstant.WBX_SITE),mk);
        serverInterface.getMeetingLink(webexAccount,
                new Callback<MeetingLinkResponse>() {
                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(TAG, " getMeetingLink  error" + error
                                + " getBody " + error.getBody());
                        AndroidUtil.closeDialog(pd);
                    }

                    @Override
                    public void success(MeetingLinkResponse response,
                            Response arg1) {
                        Log.d(TAG, " get MeetingLinkResponse " + response);
                        AndroidUtil.closeDialog(pd);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(response.getResult().getUrl()));
                        activity.startActivity(intent);
                    }
                });
    }


    public boolean notSupportWebex(Activity activity) {
        String MK= DataUtil.readFromPreferences(activity, WebexAPIConstant.WBX_PMR_MEETING_KEY);
        if (AndroidUtil.notInstallWebex(activity)){
            //AndroidUtil.showLongToast(this, getString(R.string.needInstallWebex));
            //AndroidUtil.startLink(this,SkyProductManager.getInstances().getPMRLink(this));
            DownloadUtil.downloadWebex(activity);
            return true  ;
        }
        if (DataUtil.isEmpty(MK)){
            AndroidUtil.showLongToast(activity, activity.getString(R.string.needBindWebex));
            activity.startActivity(new Intent(activity, BindWebexActivity.class));     
            return true ;
        }
        return false ;
        
    }
    

}
