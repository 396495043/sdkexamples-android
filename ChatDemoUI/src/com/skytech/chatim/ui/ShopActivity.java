package com.skytech.chatim.ui;

import java.net.URLEncoder;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.activity.BaseActivity;
import com.skytech.chatim.proxy.SkyProductManager;
import com.skytech.chatim.proxy.SkyUserManager;

public class ShopActivity extends BaseActivity {
    private static String TAG = ShopActivity.class.getSimpleName();
    private String url ="http://l.ketianyun.com/auth?token=" ;
	private WebView webView;
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.sky_shop);
        webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl(url+URLEncoder.encode(SkyUserManager.getInstances().getCiToken()));
        
        webView.setWebViewClient(new WebViewClient() {  
            // Load opened URL in the application instead of standard browser  
            // application  
            public boolean shouldOverrideUrlLoading(WebView view, String url) {  
                view.loadUrl(url);  
                return true;  
            }  
        });  
      
        webView.setWebChromeClient(new WebChromeClient() {  
            // Set progress bar during loading  
            public void onProgressChanged(WebView view, int progress) {  
            	ShopActivity.this.setProgress(progress * 100);  
            }  
        });  
      
        // Enable some feature like Javascript and pinch zoom  
        WebSettings websettings = webView.getSettings();  
        websettings.setJavaScriptEnabled(true);     // Warning! You can have XSS vulnerabilities!  
        websettings.setBuiltInZoomControls(true);  
    }
}
