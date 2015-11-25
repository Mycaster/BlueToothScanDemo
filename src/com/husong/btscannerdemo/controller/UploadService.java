package com.husong.btscannerdemo.controller;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import com.husong.btscannerdemo.fragment.OnProgressListener;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class UploadService extends Service {

   private String TAG ="UploadService"; 
   private PrintWriter out = null;
   private Socket clientSocket= null; 
   private Timer timer;
   private TimerTask timerTask;
   private SharedPreferences mysp;
   
   private static int intCounter=1;
   
   @Override
   public void onCreate(){
       super.onCreate();
       mysp = getSharedPreferences("test",Context.MODE_MULTI_PROCESS);
       Log.i("Create Service", "onCreate");
	   Log.i(TAG, "等待定时执行上传任务");
   }

   @Override
   public void onDestroy(){
       super.onDestroy();
       if(clientSocket.isConnected()){
    	   disConnect();
       }
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
       StringBuilder send_content=new StringBuilder("upload\n");
		try {
			send_content.append(Tools.ReadFromFile("blueToothScan_data.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		send_content.append("exit\n");
       	out.println(send_content);
       	updateUI("第"+(intCounter-1)+"次数据已发送\n");
       	Tools.clearFile("blueToothScan_data.txt");
       	Log.i("send status", "第"+(intCounter-1)+"次数据已发送->");
   }
   
    public void disConnect(){
      	try {
			clientSocket.shutdownOutput();
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
   
    private StringBuilder uploadInfo = new StringBuilder();
    private OnProgressListener onProgressListener;
    public void setOnProgressListener(OnProgressListener onProgressListener) {
    	this.onProgressListener = onProgressListener;
    }
    public String getProgress() {
		return uploadInfo.toString();
    }
	@Override
	public IBinder onBind(Intent arg0) {
		return new MsgBinder();
	}
	public class MsgBinder extends Binder{
		public UploadService getService(){
			return UploadService.this;
		}
	}
	
	@SuppressWarnings("deprecation")
	public void startUpload() {
		timer = new Timer(true);
		timerTask = new TimerTask(){  
			 public void run() {  //另开的线程，不在UI线程里
				 if(intCounter <= mysp.getInt("UploadCount", 0)){
					if(connect()){
						updateUI("服务器连接成功->");
						++intCounter;
						sendMessage();
					}else {
						updateUI("服务器连接失败");
						intCounter =1;
						//onDestroy();
						timer.cancel();
						timerTask.cancel();
					}
				 }else {
					 updateUI("全部发送完成");
					 intCounter = 1;
					 timer.cancel();
					 timerTask.cancel();
					 Log.i("scan","Service is Destoryed");
				 }
			 }
		};
		Date startDate =new Date();
	    startDate.setHours(mysp.getInt("StartUploadHour", 0));
	    startDate.setMinutes(mysp.getInt("StartUploadMin", 0));
	    Log.i("上传时间",startDate.getHours()+":"+startDate.getMinutes());
	    timer.schedule(timerTask,startDate,mysp.getInt("UploadInterval", 0)*1000);
		//timer.scheduleAtFixedRate(timerTask,1000,mysp.getInt("UploadInterval", 0)*1000);
		updateUI("上传进度:\n");
	}
	
	public void stopUpload(){
		timer.cancel();
		timerTask.cancel();
		if(clientSocket.isConnected()){
			disConnect();
		}
		intCounter = 1;
		updateUI("停止");
	}
	private void updateUI(String str){
		if(!str.equals("停止")&&!str.equals("服务器连接失败")){//正常情况下更新UI
			uploadInfo.append(str);
			if(onProgressListener != null){
				onProgressListener.onProgress(uploadInfo.toString());
			}	
		}else if(!str.equals("服务器连接失败")){//按下停止的时候更新UI
			uploadInfo.delete(0,uploadInfo.length());
			//uploadInfo.append("上传进度:\n");
			if(onProgressListener != null){
				onProgressListener.onProgress(uploadInfo.toString());
			}
		}else {//服务器连接失败时候更新UI
			uploadInfo.delete(0,uploadInfo.length());
			uploadInfo.append("上传进度:\n请检查服务器连接");
			if(onProgressListener != null){
				onProgressListener.onProgress(uploadInfo.toString());
			}
		}
	}
}