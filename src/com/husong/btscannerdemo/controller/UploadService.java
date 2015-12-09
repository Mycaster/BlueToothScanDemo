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
   private SharedPreferences mysp,status_sp;
   private SharedPreferences.Editor status_editor;
   private static int intCounter=1;
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
   
   @Override
   public void onCreate(){
       super.onCreate();
       mysp = getSharedPreferences("config",Context.MODE_MULTI_PROCESS);
       status_sp = getSharedPreferences("status",Context.MODE_MULTI_PROCESS);
       status_editor = status_sp.edit();
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
   
   /*
    * 连接服务器操作:
    * 获得clientSocket 和 OutputStream
    */
   public boolean connect(){
   	   try{ 
   		    String ip = mysp.getString("ip", null);
   		    int port = mysp.getInt("port", 0);
   		    clientSocket = new Socket(ip,port);
			Log.i("Socket Connect", "连接成功");
			//获得输入流
	   		out = new PrintWriter(new BufferedWriter(  
                   new OutputStreamWriter(clientSocket.getOutputStream(),"UTF-8")),true);
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
   
   /*
    * 对发送数据进行简单封装,数据格式为:
    * upload
    * data....
    * exit
    */
   public void sendMessage(){
       StringBuilder send_content=new StringBuilder("upload\n");
		try {
			send_content.append(Tools.ReadFromFile("blueToothScan_data.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		send_content.append("exit");
       	out.println(send_content);
       	updateUI("第"+(intCounter-1)+"次数据已发送\n");
       	Tools.clearFile("blueToothScan_data.txt");//清空文件缓存
       	Log.i("send status", "第"+(intCounter-1)+"次数据已发送->");
        //最后一次上传结束更新UI
		if((intCounter-1)==mysp.getInt("UploadCount", 0)){
			Log.i("status", "最后一次上传");
			status_editor.putBoolean("isUploading", false);
			if(onProgressListener != null){
				onProgressListener.onProgress("上传结束");
			}
			updateUI("全部发送完成\n");
			intCounter = 1;
			timer.cancel();
			timerTask.cancel();
			Log.i("scan","Service is Destoryed");
		}
   }
   
   
   public void disConnect(){
      	try {
			clientSocket.shutdownOutput();
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	@SuppressWarnings("deprecation")
	public void startUpload() {
		timer = new Timer(true);
		timerTask = new TimerTask(){  
			 public void run() {  //另开的线程，不在UI线程里
				 if(intCounter <= mysp.getInt("UploadCount", 0)){
					 //第一次发送时，更新按钮UI
					 if(intCounter==1){
						status_editor.putBoolean("isUploading", true);
						if(onProgressListener != null){
							onProgressListener.onProgress("正在上传");
						}
					 }
					//上传过程中更新UI
					if(connect()){
						updateUI("服务器连接成功->");
						++intCounter;
						sendMessage();
					}else{			//连接失败只更新UI不发送数据，并且下次上传会上传上次数据
						updateUI("服务器连接失败");
						timer.cancel();
						timerTask.cancel();
						++intCounter;
					}
				 }
			 }
		};
		Date startDate =new Date();
	    startDate.setHours(mysp.getInt("StartUploadHour", 0));
	    startDate.setMinutes(mysp.getInt("StartUploadMin", 0));
	    Log.i("上传时间",startDate.getHours()+":"+startDate.getMinutes());
	    timer.schedule(timerTask,startDate,mysp.getInt("UploadInterval", 0)*1000);
		updateUI("上传进度:\n");
	}
	
	public void stopUpload(){
		status_editor.putBoolean("isUploading", false);
		timer.cancel();
		timerTask.cancel();
		if(clientSocket.isConnected()){
			disConnect();
		}
		intCounter = 1;
		updateUI("停止");
	}
	
	private void updateUI(String str){
		if(onProgressListener != null){
			//更新文本框中的上传进度
			if(!str.equals("停止")&&!str.equals("服务器连接失败")){	//正常情况下更新UI
				uploadInfo.append(str);
				onProgressListener.onProgress(uploadInfo.toString());
			}else if(!str.equals("服务器连接失败")){					//按下停止的时候更新UI
				uploadInfo.delete(0,uploadInfo.length());
				onProgressListener.onProgress(uploadInfo.toString());
			}else {												//服务器连接失败时候更新UI
				uploadInfo.delete(0,uploadInfo.length());
				uploadInfo.append("上传进度:\n请检查服务器连接");
				onProgressListener.onProgress(uploadInfo.toString());
			}
		}
	}
}