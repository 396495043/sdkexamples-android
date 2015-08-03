package com.skytech.chatim.ui;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.activity.BaseActivity;

public class AboutActivity extends BaseActivity {
    private static String TAG = AboutActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.sky_activity_about);
        TextView versionText =  ((TextView) findViewById(R.id.tv_version));
        versionText.setText(versionText.getText() + getVersion());
    }

	/**
	 * 获取当前应用程序的版本号
	 */
	private String getVersion() {
		String st = getResources().getString(R.string.Version_number_is_wrong);
		PackageManager pm = getPackageManager();
		try {
			PackageInfo packinfo = pm.getPackageInfo(getPackageName(), 0);
			String version = packinfo.versionName;
			return version;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return st;
		}
	}
}
