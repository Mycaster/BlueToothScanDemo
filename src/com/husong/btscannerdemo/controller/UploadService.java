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
import com.husong.btscannerdemo.fragment.ScanFragment;

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
   private TimerTask uploadTask;
   private SharedPreferences mysp,status_sp;
   private SharedPreferences.Editor myeditor ,status_editor;
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
       myeditor = mysp.edit();
       status_sp = getSharedPreferences("status",Context.MODE_MULTI_PROCESS);
       status_editor = status_sp.edit();
       Log.i("Create Service", "onCreate");
	   Log.i(TAG, "�ȴ���ʱִ���ϴ�����");
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
    * ���ӷ���������:
    * ���clientSocket �� OutputStream
    */
   public boolean connect(){
   	   try{ 
   		    String ip = mysp.getString("ip", null);
   		    int port = mysp.getInt("port", 0);
   		    clientSocket = new Socket(ip,port);
			Log.i("Socket Connect", "���ӳɹ�");
			//���������
	   		out = new PrintWriter(new BufferedWriter(  
                   new OutputStreamWriter(clientSocket.getOutputStream(),"UTF-8")),true);
	   		return true;
       }catch (UnknownHostException e){
    	   e.printStackTrace();
			Log.i("Socket Connect", "����ʧ��1");
			return false;
       }catch (IOException e){
           e.printStackTrace();
			Log.i("Socket Connect", "����ʧ��2");
			return false;
       }
   }
   
   /*
    * �Է������ݽ��м򵥷�װ,���ݸ�ʽΪ:
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
       	updateUI("��"+(intCounter-1)+"�������ѷ���\n");
       	Tools.clearFile("blueToothScan_data.txt");//����ļ�����
       	Log.i("send status", "��"+(intCounter-1)+"�������ѷ���->");
        //���һ���ϴ���������UI
		if((intCounter-1)==mysp.getInt("UploadCount", 0)){
			Log.i("status", "���һ���ϴ�");
			status_editor.putBoolean("isUploading", false);
			status_editor.commit();
			if(onProgressListener != null){
				onProgressListener.onProgress("�ϴ�����");
			}
			updateUI("ȫ���������\n");
			intCounter = 1;
			timer.cancel();
			uploadTask.cancel();
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
		Date uploadDate = new Date(mysp.getLong("StartUploadTime", 0));
		if(new Date().after(uploadDate)){//�Ѿ����˿�ʼʱ�䣬������ִ��
			uploadDate = new Date();
			myeditor.putLong("nextUploadTime", uploadDate.getTime());
			myeditor.commit();
		}
	    Log.i("�ϴ�ʱ��",uploadDate+"");
	    timer = new Timer(true);
		uploadTask =taskGenerator();
	    timer.schedule(uploadTask,uploadDate,60*1000);
		updateUI("�ϴ�����:\n");
	}
   
   public TimerTask taskGenerator(){
	   	return new TimerTask(){  
			 @SuppressWarnings("deprecation")
			public void run() {  //�����̣߳�����UI�߳���,���Բ�����ʾ����
				 //�жϵ�ǰʱ���Ƿ����ϴ���ʱ����Ǻϣ�����ʼ�ϴ�
				 //nextTime��ʼֵΪ��ʼ��ʱ��㣬֮�����ʱ�������ϸ�����һ���ϴ���ʱ���
			 	if(Tools.calculateTime(new Date(),new Date(mysp.getLong("nextUploadTime", 0)))<30*1000){
					 //��һ�η���ʱ�����°�ťUI
					 if(intCounter==1){
						status_editor.putBoolean("isUploading", true);
						status_editor.commit();
						if(onProgressListener != null){
							onProgressListener.onProgress("�����ϴ�");
						}
					 }
					//�ϴ������и���UI
					if(connect()){
						updateUI("���������ӳɹ�->");
						++intCounter;
						sendMessage();
					}else{			//����ʧ��ֻ����UI���������ݣ������´��ϴ����ϴ��ϴ�����
						updateUI("����������ʧ��");
						timer.cancel();
						uploadTask.cancel();
						++intCounter;
					}
					
					/*
					 * ������һ�ε�ɨ��ʱ�䲢������
					 */
					Date nextTime = new Date();
					if(Tools.calculateTime(new Date(),new Date(mysp.getLong("EndUploadTime", 0)))<30*1000){
						//�����ǰʱ�������һ��ɨ���ʱ��㣬����һ��ɨ��ʱ���ɵڶ���Ŀ�ʼ�� 
						nextTime.setTime(nextTime.getTime()+mysp.getLong("StartUploadTime", 0)+2*ScanFragment.PERIOD_DAY-mysp.getLong("EndUploadTime", 0));
						intCounter=1;
						Log.i("��һ������ʱ��",nextTime+"");
						Log.i("scan","���������ȡ���ˣ�����������Ѿ��趨��");
					}else{
						//��������������һ��ɨ���ʱ���
						nextTime.setSeconds(nextTime.getSeconds()+mysp.getInt("UploadInterval", 0));
					}
					myeditor.putLong("nextUploadTime", nextTime.getTime());
					myeditor.commit();
				 }
			}
	   	};
    }

	
	public void stopUpload(){
		status_editor.putBoolean("isUploading", false);
		status_editor.commit();
		timer.cancel();
		uploadTask.cancel();
		if(clientSocket.isConnected()){
			disConnect();
		}
		intCounter = 1;
		updateUI("ֹͣ");
	}
	
	private void updateUI(String str){
		if(onProgressListener != null){
			//�����ı����е��ϴ�����
			if(!str.equals("ֹͣ")&&!str.equals("����������ʧ��")){	//��������¸���UI
				uploadInfo.append(str);
				onProgressListener.onProgress(uploadInfo.toString());
			}else if(!str.equals("����������ʧ��")){					//����ֹͣ��ʱ�����UI
				uploadInfo.delete(0,uploadInfo.length());
				onProgressListener.onProgress(uploadInfo.toString());
			}else {												//����������ʧ��ʱ�����UI
				uploadInfo.delete(0,uploadInfo.length());
				uploadInfo.append("�ϴ�����:\n�������������");
				onProgressListener.onProgress(uploadInfo.toString());
			}
		}
	}
}