package com.husong.btscannerdemo.fragment;

import android.app.Application;
import android.util.Log;

public class MyApplication extends Application {
	

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("MyApplication ", "MyApplication On Create()");
	}


} 
