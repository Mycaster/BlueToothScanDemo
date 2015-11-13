//package com.husong.btscannerdemo.fragment;
//
//import com.husong.btscannerdemo.R;
//import com.husong.btscannerdemo.controller.DateTimePickDialogUtil;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.EditText;
//
//public class DatePickActivity extends Activity {
//		/** Called when the activity is first created. */
//		private EditText startDateTime;
//		private EditText endDateTime;
//
//		private String initStartDateTime = "2013��9��3�� 14:44"; // ��ʼ����ʼʱ��
//		private String initEndDateTime = "2014��8��23�� 17:44"; // ��ʼ������ʱ��
//
//		@Override
//		public void onCreate(Bundle savedInstanceState) {
//			super.onCreate(savedInstanceState);
//			//setContentView(R.layout.main);
//
//			// ���������
//			//startDateTime = (EditText) findViewById(R.id.inputDate);
//			//endDateTime = (EditText) findViewById(R.id.inputDate2);
//
//			startDateTime.setText(initStartDateTime);
//			endDateTime.setText(initEndDateTime);
//
//			startDateTime.setOnClickListener(new OnClickListener() {
//				public void onClick(View v) {
//					DateTimePickDialogUtil dateTimePicKDialog = new DateTimePickDialogUtil(
//							DatePickActivity.this, initEndDateTime);
//					dateTimePicKDialog.dateTimePicKDialog(startDateTime);
//
//				}
//			});
//
//			endDateTime.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View arg0) {
//					DateTimePickDialogUtil dateTimePicKDialog = new DateTimePickDialogUtil(
//							DatePickActivity.this, initEndDateTime);
//					dateTimePicKDialog.dateTimePicKDialog(endDateTime);
//				}
//			});
//		}
//
//}