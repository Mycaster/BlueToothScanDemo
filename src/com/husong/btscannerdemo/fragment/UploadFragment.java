package com.husong.btscannerdemo.fragment;

import com.husong.btscannerdemo.R;
import com.husong.btscannerdemo.controller.Tools;
import com.husong.btscannerdemo.controller.UploadService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class UploadFragment extends Fragment{
	private EditText ip_edit;
    private EditText port_edit;
    private TextView detail_tx,progress_detail;
    private Button settingBt;
    private Button bt_Stop;
	private Button bt_Send;
	private Button bt_upStart,bt_upEnd;
	private EditText uploadInterval;
	private String TAG = "UploadFragment";
	private StringBuilder uploadInfo = new StringBuilder();
	
	private SharedPreferences MyPreferences;
	private SharedPreferences.Editor editor;
	
	private Activity main;
	private Intent i;
	private UploadService myservice;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
    	MyPreferences = getActivity().getSharedPreferences("test",Context.MODE_MULTI_PROCESS);
        editor = MyPreferences.edit();
        main = this.getActivity();
        //绑定Service
    	i = new Intent(main, UploadService.class);
//        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        main.bindService(i, conn, Context.BIND_AUTO_CREATE);
        Log.i("Activity Message ", main+" in Fragment");
        
        //控件布局初始化
        View rootView = inflater.inflate(R.layout.upload, container, false);//关联布局文件  
    	bt_Send = (Button)rootView.findViewById(R.id.bt_send);
        bt_Stop = (Button)rootView.findViewById(R.id.bt_stop);
        bt_Stop.setEnabled(false);
        bt_Stop.setTextColor(Color.GRAY);
        bt_upStart = (Button)rootView.findViewById(R.id.setStartUploadBt);
        bt_upEnd = (Button)rootView.findViewById(R.id.setEndUploadBt);
        settingBt = (Button)rootView.findViewById(R.id.setting_bt);
        uploadInterval  = (EditText)rootView.findViewById(R.id.edit_tx);
    	ip_edit = (EditText)rootView.findViewById(R.id.ip_edit);
    	port_edit =(EditText)rootView.findViewById(R.id.port_edit);
    	detail_tx = (TextView)rootView.findViewById(R.id.detail_tx);
    	progress_detail = (TextView)rootView.findViewById(R.id.uploadDetail);
    	uploadInfo.append("上传进度:\n");
    	progress_detail.setText(uploadInfo);
    	Tools.updateDisplayInfo(detail_tx);

    	bt_upStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Tools.dateTimePicKDialog("设置起始上传时间",detail_tx);
				Tools.updateDisplayInfo(detail_tx);
			}
		});
    	
    	bt_upEnd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Tools.dateTimePicKDialog("设置结束上传时间",detail_tx);
				Tools.updateDisplayInfo(detail_tx);
				
			}
		});
    	
        settingBt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(!ip_edit.getText().toString().equals("")){
					editor.putString("ip", ip_edit.getText().toString());
					editor.commit();
					ip_edit.setText("");
					Tools.updateDisplayInfo(detail_tx);
				}
				if(!port_edit.getText().toString().equals("")){
					int port = Integer.parseInt(port_edit.getText().toString());
					editor.putInt("port", port);
					editor.commit();
					port_edit.setText("");
					Tools.updateDisplayInfo(detail_tx);
				}
				if(!uploadInterval.getText().toString().equals("")){
					int interval = Integer.parseInt(uploadInterval.getText().toString());
					editor.putInt("UploadInterval", interval);
					editor.commit();
					uploadInterval.setText("");
					Tools.updateDisplayInfo(detail_tx);
				}
			}
		});
        
        bt_Send.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//改变界面状态
				uploadInfo.delete(0, uploadInfo.length());
				uploadInfo.append("上传进度:\n");
				progress_detail.setText(uploadInfo);
                bt_Send.setEnabled(false);
                bt_Send.setTextColor(Color.GRAY);
                bt_Stop.setEnabled(true);
                bt_Stop.setTextColor(Color.WHITE);
		        //开始上传
		        myservice.startUpload();
                Log.i("UploadFragment", "开始上传");
			}
		});
        
        bt_Stop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				myservice.stopUpload();
				bt_Send.setEnabled(true);
                bt_Send.setTextColor(Color.WHITE);
                bt_Stop.setEnabled(false);
                bt_Stop.setTextColor(Color.GRAY);
                Log.i("UploadFragment", "结束上传");
			}
		});
    	return rootView;
    }
    
    ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
		}
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			//返回一个MsgService对象
			myservice = ((UploadService.MsgBinder)service).getService();
			//注册回调接口来接收下载进度的变化
			myservice.setOnProgressListener(new OnProgressListener() {
				@Override
				public void onProgress(String progress) {
					Message msg = new Message();
					msg.obj = progress;
					mHandler.sendMessage(msg);
				}
			});	
		}
	};
	
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			String text = msg.obj.toString();
			if(text.equals("上传进度:\n")){
                bt_Send.setEnabled(true);
                bt_Send.setTextColor(Color.WHITE);
                bt_Stop.setEnabled(false);
                bt_Stop.setTextColor(Color.GRAY);
			}
			progress_detail.setText(text);
		}
		
	};
}
