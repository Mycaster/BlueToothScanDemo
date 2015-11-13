package com.husong.btscannerdemo.fragment;

import java.util.Date;
import android.app.Application;
import android.content.Context;
import android.util.Log;

public class MyApplication extends Application {
	

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.i("MyApplication ", "MyApplication On Create()");
	}


} 
