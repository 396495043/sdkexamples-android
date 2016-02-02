package com.skytech.chatim.ui;

import java.text.SimpleDateFormat;

import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;

import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.activity.BaseActivity;

public class SignInActivity extends BaseActivity {
    private static String TAG = SignInActivity.class.getSimpleName();
	private CalendarView calendar;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.sky_signin);
 
        calendar = (CalendarView) findViewById(R.id.calendarView);  
        Long nowTime = calendar.getDate();  
        SimpleDateFormat f = new SimpleDateFormat("yyyy年MM月dd日hh:mm:ss");  
        String time = f.format(nowTime);  
        System.out.println("-------------" + time);  
        calendar.setOnDateChangeListener(new OnDateChangeListener() {  
            @Override  
            public void onSelectedDayChange(CalendarView arg0, int arg1,  
                    int arg2, int arg3) {  
                arg2 = arg2 + 1;  
                Log.d(TAG," CalendarView -------------" + arg1 + "-" + arg2 + "-"  
                        + arg3);  
            }  
        });  
    }


}
