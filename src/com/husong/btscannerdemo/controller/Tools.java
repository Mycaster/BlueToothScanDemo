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
import com.husong.btscannerdemo.R;
import com.husong.btscannerdemo.fragment.MenuActivity;
import com.husong.btscannerdemo.fragment.MyApplication;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

public class Tools {
	private static MenuActivity ma =  MenuActivity.getInstance();
	private static SharedPreferences MyPreferences = ma.getSharedPreferences("test",Context.MODE_MULTI_PROCESS);
    private static SharedPreferences.Editor editor = MyPreferences.edit();
	
    public static  void dateTimePicKDialog(final String title,final TextView info) {
		
		LinearLayout dateTimeLayout = (LinearLayout)ma.getLayoutInflater().inflate(R.layout.timepicker, null);
		final TimePicker timepicker = (TimePicker) dateTimeLayout.findViewById(R.id.timepicker);
		timepicker.setIs24HourView(true);
		timepicker.setOnTimeChangedListener(new OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker arg0, int arg1, int arg2) {
				// TODO Auto-generated method stub
			}
		});
		AlertDialog ad = new AlertDialog.Builder(ma)
				.setTitle(title)
				.setView(dateTimeLayout)
				.setPositiveButton("����", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if(title.equals("������ʼʱ��")){
							editor.putInt("StartScanHour", timepicker.getCurrentHour());
				        	editor.putInt("StartScanMin", timepicker.getCurrentMinute());
				        	editor.commit();
				        	Log.i("editor", timepicker.getCurrentHour()+":"+timepicker.getCurrentMinute());
				        	Log.i("editor", "commit");
				        	updatescanDetail(info);
						}else if(title.equals("���ý���ʱ��")){
				        	editor.putInt("EndScanHour", timepicker.getCurrentHour());
				        	editor.putInt("EndScanMin", timepicker.getCurrentMinute());
				        	editor.commit();
				        	Log.i("editor2", timepicker.getCurrentHour()+":"+timepicker.getCurrentMinute());
				        	Log.i("editor2", "commit");
				        	updatescanDetail(info);
						}else if(title.equals("������ʼ�ϴ�ʱ��")){
							editor.putInt("StartUploadHour", timepicker.getCurrentHour());
				        	editor.putInt("StartUploadMin", timepicker.getCurrentMinute());
				        	editor.commit();
				        	Log.i("editor", timepicker.getCurrentHour()+":"+timepicker.getCurrentMinute());
				        	Log.i("editor", "commit");
				        	updateDisplayInfo(info);
						}else if(title.equals("���ý����ϴ�ʱ��")){
				        	editor.putInt("EndUploadHour", timepicker.getCurrentHour());
				        	editor.putInt("EndUploadMin", timepicker.getCurrentMinute());
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
		// TODO Auto-generated method stub
    	int s_hour = MyPreferences.getInt("StartScanHour", 0);
    	int s_min = MyPreferences.getInt("StartScanMin",0);
    	int e_hour = MyPreferences.getInt("EndScanHour", 0);
    	int e_min = MyPreferences.getInt("EndScanMin", 0);
    	int s_interval = MyPreferences.getInt("ScanInterval", 0);
    	int TotalTime = Tools.calculateTime(s_hour,s_min,e_hour,e_min);
    	int s_count = TotalTime*60/s_interval;
    	editor.putInt("ScanCount", s_count);
    	editor.commit();
    	scaninfo.setText("��ǰ����Ϊ: \nɨ��ʱ��:  "+s_hour+":"+s_min+"��"+e_hour+":"+e_min+
				"\nɨ����: "+s_interval+	"��\nɨ�����: "+s_count);
	}
	
	public static void updateDisplayInfo(TextView detail_tx) {
		int TotalTime = Tools.calculateTime(MyPreferences.getInt("StartUploadHour", 0),MyPreferences.getInt("StartUploadMin",0),
			     					  MyPreferences.getInt("EndUploadHour", 0),MyPreferences.getInt("EndUploadMin", 0));
		editor.putInt("UploadCount", TotalTime*60/(MyPreferences.getInt("UploadInterval", 0)));
    	editor.commit();
		String displayInfo = "  ��ǰ����Ϊ�� \n  IP��ַ:		"+MyPreferences.getString("ip",null)+
					"\n  �˿ں�:		"+MyPreferences.getInt("port", 0)+
					"\n  ʱ����:		"+MyPreferences.getInt("UploadInterval", 0)+"��"+
					"\n  �ϴ�ʱ��:		"+MyPreferences.getInt("StartUploadHour", 0)+
							": "+MyPreferences.getInt("StartUploadMin", 0)+"��"+MyPreferences.getInt("EndUploadHour", 0)+
							": "+MyPreferences.getInt("EndUploadMin", 0)+
					"\n  �ϴ�����:		"+MyPreferences.getInt("UploadCount", 0);
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
		try {
			File file = new File(SDPATH, fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			OutputStream out = new FileOutputStream(file, true);
			out.write("".getBytes());
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * ��ȡϵͳ��ǰʱ��
	 */
	public static String getCurrentTime(){
		Date date=new Date();
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return df.format(date);
	}

	/*
	 * ����ʱ���
	 */
   public static int calculateTime(int StartHour,int StartMin,int EndHour,int EndMin){
	   if(StartHour <= EndHour){
		   if(EndMin >= StartMin){
			   return (EndHour-StartHour)*60+ EndMin-StartMin;
		   }else{
			   return (EndHour-StartHour)*60+ StartMin-EndMin;
		   }
	   }
	   return 0; 
   }
   
   /*
    * ��ʽ��ʱ��
    */
   public static String format(int a1,int a2,int a3,int a4){
		Date date1= new Date();
		Date date2 = new Date();
		date1.setHours(a1);
		date1.setMinutes(a2);
		date2.setHours(a3);
		date2.setMinutes(a4);
		SimpleDateFormat df=new SimpleDateFormat("HH:MM");
		return df.format(date1)+"-"+df.format(date2);
   }

}