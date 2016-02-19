package com.skytech.chatim.ui;

import java.util.Calendar;

import android.os.Bundle;
import android.util.Log;

import com.andexert.calendarlistview.library.DayPickerView;
import com.andexert.calendarlistview.library.SimpleMonthAdapter;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.activity.BaseActivity;

public class SignInActivity extends BaseActivity implements
		com.andexert.calendarlistview.library.DatePickerController {
	private static String TAG = ShopActivity.class.getSimpleName();
	private DayPickerView dayPickerView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sky_signin);
		dayPickerView = (DayPickerView) findViewById(R.id.calendarlistview);
		dayPickerView.setController(this);
		Log.d(TAG, "Day Selected ");
	}

	@Override
	public int getMaxYear() {
		 Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.YEAR);
	}

	@Override
	public void onDayOfMonthSelected(int year, int month, int day) {
		Log.d(TAG, "Day Selected " + day + " / " + month + " / " + year);
	}

	@Override
	public void onDateRangeSelected(
			SimpleMonthAdapter.SelectedDays<SimpleMonthAdapter.CalendarDay> selectedDays) {

		Log.d(TAG, "Date range selected " + selectedDays.getFirst().toString()
				+ " --> " + selectedDays.getLast().toString());
	}
}
