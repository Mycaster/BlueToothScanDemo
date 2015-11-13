package com.husong.btscannerdemo.controller;

import java.util.Calendar;
import com.husong.btscannerdemo.R;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;


public class DateTimePickDialogUtil implements OnTimeChangedListener {
	
	private TimePicker timePicker;
	private AlertDialog ad;
	private String dateTime;
	private Fragment fragment;
	//private Calendar calendar;
	private Time time= new Time();

	public DateTimePickDialogUtil(Fragment fg) {
		this.fragment = fg;
		time.hour = Calendar.getInstance().getTime().getHours();
		time.minute = Calendar.getInstance().getTime().getMinutes();
	}
	public void dateTimePicKDialog(String title) {
		LinearLayout dateTimeLayout = (LinearLayout) fragment.getLayoutInflater(null).inflate(R.layout.timepicker, null);
		timePicker = (TimePicker) dateTimeLayout.findViewById(R.id.timepicker);
		timePicker.setIs24HourView(true);
		timePicker.setOnTimeChangedListener(this);
		ad = new AlertDialog.Builder(fragment.getActivity())
				.setTitle(title)
				.setView(dateTimeLayout)
				.setPositiveButton("设置", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						
						Log.i("设置", "设置");
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				}).show();
		Log.i("Time", time.hour+":"+time.minute);
	}
	@Override
	public void onTimeChanged(TimePicker arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

}
