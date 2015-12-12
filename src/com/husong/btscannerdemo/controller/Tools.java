package com.husong.btscannerdemo.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import com.husong.btscannerdemo.R;
import com.husong.btscannerdemo.fragment.MenuActivity;

@SuppressLint({ "SimpleDateFormat", "InflateParams" }) 
public class Tools {
	private static MenuActivity ma =  MenuActivity.getInstance();
	private static SharedPreferences msp = ma.getSharedPreferences("config",Context.MODE_MULTI_PROCESS);
    private static SharedPreferences.Editor editor = msp.edit();
    
    public static  void dateTimePicKDialog(final String title,final TextView info) {
		LinearLayout dateTimeLayout = (LinearLayout)ma.getLayoutInflater().inflate(R.layout.timepicker, null);
		final TimePicker timepicker = (TimePicker) dateTimeLayout.findViewById(R.id.timepicker);
		timepicker.setIs24HourView(true);
		timepicker.setOnTimeChangedListener(new OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker arg0, int arg1, int arg2) {
			}
		});
		AlertDialog ad = new AlertDialog.Builder(ma)
				.setTitle(title)
				.setView(dateTimeLayout)
				.setPositiveButton("����", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if(title.equals("������ʼʱ��")){
//							timepicker.setCurrentHour(new Date(msp.getLong("StartScanTime", 0)).getHours());
//							timepicker.setCurrentMinute(new Date(msp.getLong("StartScanTime", 0)).getMinutes());
							Date date = new Date();
							date.setHours(timepicker.getCurrentHour());
							date.setMinutes(timepicker.getCurrentMinute());
							editor.putLong("StartScanTime", date.getTime());
				        	editor.commit();
				        	Log.i("editor", timepicker.getCurrentHour()+":"+timepicker.getCurrentMinute());
				        	Log.i("editor", "commit");
				        	updatescanDetail(info);
						}else if(title.equals("���ý���ʱ��")){
//							timepicker.setCurrentHour(new Date(msp.getLong("EndScanTime", 0)).getHours());
//							timepicker.setCurrentMinute(new Date(msp.getLong("EndScanTime", 0)).getMinutes());
							Date date = new Date();
							date.setHours(timepicker.getCurrentHour());
							date.setMinutes(timepicker.getCurrentMinute());
							editor.putLong("EndScanTime", date.getTime());
				        	editor.commit();
				        	Log.i("editor2", timepicker.getCurrentHour()+":"+timepicker.getCurrentMinute());
				        	Log.i("editor2", "commit");
				        	updatescanDetail(info);
						}else if(title.equals("������ʼ�ϴ�ʱ��")){
//							timepicker.setCurrentHour(new Date(msp.getLong("StartUploadTime", 0)).getHours());
//							timepicker.setCurrentMinute(new Date(msp.getLong("StartUploadTime", 0)).getMinutes());
							Date date = new Date();
							date.setHours(timepicker.getCurrentHour());
							date.setMinutes(timepicker.getCurrentMinute());
							editor.putLong("StartUploadTime", date.getTime());
				        	editor.commit();
				        	Log.i("editor", timepicker.getCurrentHour()+":"+timepicker.getCurrentMinute());
				        	Log.i("editor", "commit");
				        	updateDisplayInfo(info);
						}else if(title.equals("���ý����ϴ�ʱ��")){
//							timepicker.setCurrentHour(new Date(msp.getLong("EndUploadTime", 0)).getHours());
//							timepicker.setCurrentMinute(new Date(msp.getLong("EndUploadTime", 0)).getMinutes());
							Date date = new Date();
							date.setHours(timepicker.getCurrentHour());
							date.setMinutes(timepicker.getCurrentMinute());
							editor.putLong("EndUploadTime", date.getTime());
				        	editor.commit();
				        	Log.i("editor2", timepicker.getCurrentHour()+":"+timepicker.getCurrentMinute());
				        	Log.i("editor2", "commit");
				        	updateDisplayInfo(info);
						}
						Log.i("����", "����");
					}
				}).setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				}).show();
	}
	
	public static void updatescanDetail(TextView scaninfo) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		Date startScanTime = new Date(msp.getLong("StartScanTime", 0));
		Date EndScanTime = new Date(msp.getLong("EndScanTime", 0));
		int s_interval = msp.getInt("ScanInterval", 0);
    	int TotalTime = (int) ((EndScanTime.getTime()-startScanTime.getTime())/1000);
    	int s_count = TotalTime/s_interval+1;
    	editor.putInt("ScanCount", s_count);
    	editor.commit();
		scaninfo.setText("ɨ��ʱ��:  "+sdf.format(startScanTime)+"-"+sdf.format(EndScanTime)+
				"\nɨ����: "+s_interval+	"��" +"\nɨ�����: "+s_count);
	}
	
	public static void updateDisplayInfo(TextView detail_tx) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		Date startUploadTime = new Date(msp.getLong("StartUploadTime", 0));
		Date EndUploadTime = new Date(msp.getLong("EndUploadTime", 0));
		int u_interval = msp.getInt("UploadInterval", 0);
    	int TotalTime = (int) ((EndUploadTime.getTime()-startUploadTime.getTime())/1000);
    	int u_count = TotalTime/u_interval+1;
    	editor.putInt("UploadCount", u_count);
    	editor.commit();
    	String displayInfo = 
				"  IP��ַ:		"+msp.getString("ip",null)+
				"\n  �˿ں�:		"+msp.getInt("port", 0)+
				"\n  ʱ����:		"+msp.getInt("UploadInterval", 0)+"��"+
				"\n  �ϴ�ʱ��:		"+sdf.format(startUploadTime)+"-"+sdf.format(EndUploadTime)+
				"\n  �ϴ�����:		"+msp.getInt("UploadCount", 0);
    	detail_tx.setText(displayInfo);
	}

	/*
	 * ���ļ���д�йز���
	 * ReadFromFile: ���ļ��ж�ȡɨ����Ϣ
	 * WriteToFile: ������ɨ�����Ϣд���ļ�
	 */
	private static final String SDPATH = "/mnt/sdcard"; 
	public static String ReadFromFile(String filename) throws IOException{
         File file=new File(SDPATH+"/"+filename);
         if(!file.exists()||file.isDirectory())
             throw new FileNotFoundException();
         BufferedReader br=new BufferedReader(new FileReader(file));
         String temp=null;
         StringBuffer sb=new StringBuffer();
         temp=br.readLine();
         while(temp!=null){
             sb.append(temp+"\n");
             temp=br.readLine();
         }
         br.close();
         return sb.toString();
     }
	
	public static void writeToFile(String fileName, String content) {
		try {
			File file = new File(SDPATH, fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			OutputStream out = new FileOutputStream(file, true);
			out.write(content.getBytes());
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void clearFile(String fileName){
		(new File(SDPATH, fileName)).delete();
	}
	
	/*
	 * ��ȡϵͳ��ǰʱ��
	 */
	@SuppressLint("SimpleDateFormat") 
	public static String getCurrentTime(){
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(new Date());
	}
   
   public static long calculateTime(Date date1 ,Date date2){
	   long l = date1.getTime()-date2.getTime();
	   Log.i("��ǰʱ��",date1+"");
	   Log.i("�´��ϴ�ʱ��",date2+"");
	   Log.i("����ʱ���",l+"");
	   return Math.abs(l);
   }

}
