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
			}
		});
		AlertDialog ad = new AlertDialog.Builder(ma)
				.setTitle(title)
				.setView(dateTimeLayout)
				.setPositiveButton("设置", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if(title.equals("设置起始时间")){
							editor.putInt("StartScanHour", timepicker.getCurrentHour());
				        	editor.putInt("StartScanMin", timepicker.getCurrentMinute());
				        	editor.commit();
				        	Log.i("editor", timepicker.getCurrentHour()+":"+timepicker.getCurrentMinute());
				        	Log.i("editor", "commit");
				        	updatescanDetail(info);
						}else if(title.equals("设置结束时间")){
				        	editor.putInt("EndScanHour", timepicker.getCurrentHour());
				        	editor.putInt("EndScanMin", timepicker.getCurrentMinute());
				        	editor.commit();
				        	Log.i("editor2", timepicker.getCurrentHour()+":"+timepicker.getCurrentMinute());
				        	Log.i("editor2", "commit");
				        	updatescanDetail(info);
						}else if(title.equals("设置起始上传时间")){
							editor.putInt("StartUploadHour", timepicker.getCurrentHour());
				        	editor.putInt("StartUploadMin", timepicker.getCurrentMinute());
				        	editor.commit();
				        	Log.i("editor", timepicker.getCurrentHour()+":"+timepicker.getCurrentMinute());
				        	Log.i("editor", "commit");
				        	updateDisplayInfo(info);
						}else if(title.equals("设置结束上传时间")){
				        	editor.putInt("EndUploadHour", timepicker.getCurrentHour());
				        	editor.putInt("EndUploadMin", timepicker.getCurrentMinute());
				        	editor.commit();
				        	Log.i("editor2", timepicker.getCurrentHour()+":"+timepicker.getCurrentMinute());
				        	Log.i("editor2", "commit");
				        	updateDisplayInfo(info);
						}
						Log.i("设置", "设置");
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				}).show();
	}
	
	public static void updatescanDetail(TextView scaninfo) {
    	int ss_hour = MyPreferences.getInt("StartScanHour", 0);
    	int ss_min = MyPreferences.getInt("StartScanMin",0);
    	int se_hour = MyPreferences.getInt("EndScanHour", 0);
    	int se_min = MyPreferences.getInt("EndScanMin", 0);
    	int s_interval = MyPreferences.getInt("ScanInterval", 0);
    	int TotalTime = Tools.calculateTime(ss_hour,ss_min,se_hour,se_min);
    	int s_count = TotalTime*60/s_interval+1;
    	editor.putInt("ScanCount", s_count);
    	editor.commit();
    	String time="";
    	if(ss_min<10 && se_min>=10){
    		time=ss_hour+":0"+ss_min+"—"+se_hour+":"+se_min;
    	}else if(ss_min<10 && se_min<10){
    		time=ss_hour+":0"+ss_min+"—"+se_hour+":0"+se_min;
    	}else if(ss_min>=10&&se_min<10){
    		time=ss_hour+":"+ss_min+"—"+se_hour+":0"+se_min;
    	}else if(ss_min>=10&&se_min>=10){
    		time=ss_hour+":"+ss_min+"—"+se_hour+":"+se_min;
    	}
    	scaninfo.setText("扫描时间:  "+time+"\n扫描间隔: "+s_interval+	"秒" +"\n扫描次数: "+s_count);
	}
	
	public static void updateDisplayInfo(TextView detail_tx) {
    	int us_hour = MyPreferences.getInt("StartUploadHour", 0);
    	int us_min = MyPreferences.getInt("StartUploadMin",0);
    	int ue_hour = MyPreferences.getInt("EndUploadHour", 0);
    	int ue_min = MyPreferences.getInt("EndUploadMin", 0);
    	int u_interval = MyPreferences.getInt("UploadInterval", 0);
		int TotalTime = Tools.calculateTime(us_hour,us_min,ue_hour,ue_min);
		int u_count = TotalTime*60/u_interval+1;
		editor.putInt("UploadCount", u_count);
    	editor.commit();
    	String time="";
    	if(us_min<10&&ue_min>=10){
    		time=us_hour+":0"+us_min+"—"+ue_hour+":"+ue_min;
    	}else if(us_min<10&&ue_min<10){
    		time=us_hour+":0"+us_min+"—"+ue_hour+":0"+ue_min;
    	}else if(us_min>=10&&ue_min<10){
    		time=us_hour+":"+us_min+"—"+ue_hour+":0"+ue_min;
    	}else if(us_min>=10&&ue_min>=10){
    		time=us_hour+":"+us_min+"—"+ue_hour+":"+ue_min;
    	}
		String displayInfo = 
				"  IP地址:		"+MyPreferences.getString("ip",null)+
				"\n  端口号:		"+MyPreferences.getInt("port", 0)+
				"\n  时间间隔:		"+MyPreferences.getInt("UploadInterval", 0)+"秒"+
				"\n  上传时间:		"+time+
				"\n  上传次数:		"+MyPreferences.getInt("UploadCount", 0);
		detail_tx.setText(displayInfo);
	}

	/*
	 * 与文件读写有关操作
	 * ReadFromFile: 从文件中读取扫描信息
	 * WriteToFile: 将蓝牙扫描的信息写入文件
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
	 * 获取系统当前时间
	 */
	@SuppressLint("SimpleDateFormat") 
	public static String getCurrentTime(){
		Date date=new Date();
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(date);

	}

	/*
	 * 计算时间差
	 */
   public static int calculateTime(int StartHour,int StartMin,int EndHour,int EndMin){
	   if(StartHour <= EndHour){
		   if((StartHour==EndHour)&&StartMin>EndMin)
			   return 0;
		   return (EndHour-StartHour)*60+ EndMin-StartMin;
	   }
	   return 0; 
   }

}
