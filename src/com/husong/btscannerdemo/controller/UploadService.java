package com.husong.btscannerdemo.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.husong.btscannerdemo.R;
import com.husong.btscannerdemo.fragment.MenuActivity;
import com.husong.btscannerdemo.fragment.MyApplication;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

public class UploadService extends Service {

   private String TAG ="UploadService"; 
   private BufferedReader in = null;  
   private PrintWriter out = null;
   private Socket clientSocket= null; 
   private Timer timer = new Timer(true);
   private SharedPreferences mysp;
   
   private static int intCounter=0;
   private int uploadInterval;
   private int uploadcount = 15;
   private Date startDate =new Date();
   
   LinearLayout uploadFragment = (LinearLayout)(MenuActivity.getInstance().getLayoutInflater().inflate(R.layout.upload, null));
   TextView Progress_Detail = (TextView)uploadFragment.findViewById(R.id.uploadDetail);
   
   private TimerTask timerTask = new TimerTask(){  
		 public void run() {  //另开的线程，不在UI线程里
			 if(intCounter<=uploadcount){
					++intCounter;
					if(connect()){
						sendMessage();
				        Log.i("Current Thread ID",""+Thread.currentThread().getId());
				   		Log.i("Run Service", "Counter:"+Integer.toString(intCounter));
					}else {
					 onDestroy();
					 Log.i("scan","Service is Destoryed");
					}
			 }
		 }
	};
   

   @Override
   public void onCreate(){
       super.onCreate();
       mysp = getSharedPreferences("test",Context.MODE_MULTI_PROCESS);
       uploadcount = mysp.getInt("UploadCount", 0);
       uploadInterval = mysp.getInt("Interval", 0);
       Log.i("Create Service", "onCreate");
   }
   
   @Override
   public void onStart(Intent intent,int startId){
	   	Log.i("Current Thread ID", ""+Thread.currentThread().getId());
	   	//timer.schedule(timerTask,1000,uploadInterval*1000);
	   	Log.i("startUploadHour", mysp.getInt("StartUploadHour",0)+"");
	   	Log.i("startUploadMin", mysp.getInt("StartUploadMin",0)+"");
	   	startDate.setHours(mysp.getInt("StartUploadHour", 0));
	   	startDate.setMinutes(mysp.getInt("StartUploadMin", 0));
	   	Log.i(TAG, "等待定时执行任务");
	   	//timer.schedule(timerTask,startDate,uploadInterval*1000);
	   	timer.schedule(timerTask,1000,uploadInterval*1000);
	   	super.onStart(intent, startId);
	   	Log.i("Start Service", "onStart");
   }

   @Override
   public void onDestroy(){
       super.onDestroy();
       disConnect();
       timer.cancel();
       Log.i("Destroy Service", "onDestroy");
   }
   
   public boolean connect(){
   	   try{ 
   		    String ip = mysp.getString("ip", null);
   		    int port = mysp.getInt("port", 0);
   		    Log.i("Address: ",ip);
   		    Log.i("Port: ",port+"");
   		    clientSocket = new Socket(ip,port);
			Log.i("Socket Connect", "连接成功");
			//获得输入流
	   		out = new PrintWriter(new BufferedWriter(  
                   new OutputStreamWriter(clientSocket.getOutputStream(),"UTF-8")),true);
	   		Log.i("Socket Connect", "获得输出流句柄");
	   		return true;
       }catch (UnknownHostException e){
    	   e.printStackTrace();
			Log.i("Socket Connect", "连接失败1");
			return false;
       }catch (IOException e){
           e.printStackTrace();
			Log.i("Socket Connect", "连接失败2");
			return false;
       }
   }
   
   public void sendMessage(){
       String send_content="";
		try {
			send_content = Tools.ReadFromFile("blueToothScan_data");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       	out.println(send_content);
       	Tools.clearFile("blueToothScan_data");
       	Log.i("send status", "数据已发送");
   }
   
   public void disConnect(){
      	try {
			clientSocket.shutdownOutput();
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   }
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}