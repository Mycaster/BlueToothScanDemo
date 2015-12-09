package com.husong.btscannerdemo.fragment;

import java.util.Date;

import com.husong.btscannerdemo.R;
import com.husong.btscannerdemo.controller.Tools;
import com.husong.btscannerdemo.controller.UploadService;
import android.annotation.SuppressLint;
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
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class UploadFragment extends Fragment{
	private View rootView;
    private Button settingBt;
    private Button bt_Stop;
	private Button bt_Send;
	private Button bt_upStart,bt_upEnd;
	private EditText ip_edit;
    private EditText port_edit;
    private TextView detail_tx,progress_detail;
	private EditText uploadInterval;
	private StringBuilder uploadInfo = new StringBuilder();
	
	private SharedPreferences config_sp,status_sp;
	private SharedPreferences.Editor config_editor;
	
	private Activity main;
	private Intent i;
	private UploadService myservice;
	
	private static final UploadFragment uploadFragment = new UploadFragment();
	

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i("onCreateView","onCreateView()");
        main = this.getActivity();
		config_sp = main.getSharedPreferences("config",Context.MODE_MULTI_PROCESS);
		config_editor = config_sp.edit();
		status_sp = main.getSharedPreferences("status",Context.MODE_MULTI_PROCESS);
        //�ؼ����ֳ�ʼ��
        if (rootView == null) {
        	 rootView = inflater.inflate(R.layout.upload, container, false);//���������ļ�  
        } else {
            ((ViewGroup)rootView.getParent()).removeView(rootView);
        }
    	bt_Send = (Button)rootView.findViewById(R.id.bt_send);
        bt_Stop = (Button)rootView.findViewById(R.id.bt_stop);
        bt_upStart = (Button)rootView.findViewById(R.id.setStartUploadBt);
        bt_upEnd = (Button)rootView.findViewById(R.id.setEndUploadBt);
        settingBt = (Button)rootView.findViewById(R.id.setting_bt);
        uploadInterval  = (EditText)rootView.findViewById(R.id.edit_tx);
    	ip_edit = (EditText)rootView.findViewById(R.id.ip_edit);
    	port_edit =(EditText)rootView.findViewById(R.id.port_edit);
    	detail_tx = (TextView)rootView.findViewById(R.id.detail_tx);
    	progress_detail = (TextView)rootView.findViewById(R.id.uploadDetail);
    	progress_detail.setMovementMethod(ScrollingMovementMethod.getInstance()) ;
    	Tools.updateDisplayInfo(detail_tx);
        //��Service
    	i = new Intent(main, UploadService.class);
        main.bindService(i, conn, Context.BIND_AUTO_CREATE);
    	
    	bt_upStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Tools.dateTimePicKDialog("������ʼ�ϴ�ʱ��",detail_tx);
				Tools.updateDisplayInfo(detail_tx);
			}
		});
    	
    	bt_upEnd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Tools.dateTimePicKDialog("���ý����ϴ�ʱ��",detail_tx);
				Tools.updateDisplayInfo(detail_tx);
			}
		});

    	settingBt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(!ip_edit.getText().toString().equals("")){
					config_editor.putString("ip", ip_edit.getText().toString());
					config_editor.commit();
					ip_edit.setText("");
					Tools.updateDisplayInfo(detail_tx);
				}
				if(!port_edit.getText().toString().equals("")){
					int port = Integer.parseInt(port_edit.getText().toString());
					config_editor.putInt("port", port);
					config_editor.commit();
					port_edit.setText("");
					Tools.updateDisplayInfo(detail_tx);
				}
				if(!uploadInterval.getText().toString().equals("")){
					int interval = Integer.parseInt(uploadInterval.getText().toString());
					config_editor.putInt("UploadInterval", interval);
					config_editor.commit();
					uploadInterval.setText("");
					Tools.updateDisplayInfo(detail_tx);
				}
				if(bt_Send.getText().equals("�ȴ��ϴ�...")){
					Toast.makeText(getActivity(), "������˼����ǰ�޸�Ҫ���´��ϴ��ǲ�����ЧŶ~", Toast.LENGTH_SHORT).show();
				}
			}
		});
        
        bt_Send.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View arg0) {
				Date startDate =new Date();
			    startDate.setHours(config_sp.getInt("StartUploadHour", 0));
			    startDate.setMinutes(config_sp.getInt("StartUploadMin", 0));
			    Date EndDate =new Date();
			    EndDate.setHours(config_sp.getInt("EndUploadHour", 0));
			    EndDate.setMinutes(config_sp.getInt("EndUploadMin", 0));
			    if(!startDate.after(new Date())){
					Toast.makeText(getActivity(), "�������ڵ�ǰʱ��֮��ִ�иò�����", Toast.LENGTH_LONG).show();
				}else if(!EndDate.after(startDate)){
					Toast.makeText(getActivity(), "����ʱ������ڿ�ʼʱ�䣡", Toast.LENGTH_LONG).show();
				}else {
					//�ı����״̬
					uploadInfo.delete(0, uploadInfo.length());
					uploadInfo.append("�ϴ�����:\n");
					progress_detail.setText(uploadInfo);
	                bt_Send.setEnabled(false);
	                bt_Send.setText("�ȴ��ϴ�...");
	                bt_Send.setTextColor(Color.BLACK);
	                bt_Stop.setEnabled(true);
	                bt_Stop.setTextColor(Color.WHITE);
			        //��ʼ�ϴ�
			        myservice.startUpload();
	                Log.i("UploadFragment", "�ȴ��ϴ�");
				}
			}
		});
        
        bt_Stop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(status_sp.getBoolean("isUploading", false)){
					myservice.stopUpload();
					bt_Send.setEnabled(true);
	                bt_Send.setTextColor(Color.WHITE);
	                bt_Stop.setEnabled(false);
	                bt_Stop.setTextColor(Color.GRAY);
	                Log.i("UploadFragment", "�����ϴ�");
				}else{
					Toast.makeText(getActivity(), "�㶼û��ʼ�����Ž�����", Toast.LENGTH_SHORT).show();
				}
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
			//����һ��MsgService����
			myservice = ((UploadService.MsgBinder)service).getService();
			//ע��ص��ӿ��������ϴ����ȵı仯
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
	
	@SuppressLint("HandlerLeak") 
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			String text = msg.obj.toString();
			if(text.equals("�����ϴ�")){
				bt_Send.setText("�����ϴ�");
			}else if(text.equals("")||text.equals("�ϴ�����:\n�������������")||text.equals("�ϴ�����")){
				bt_Send.setText("�ٴ��ϴ�");
                bt_Send.setEnabled(true);
                bt_Send.setTextColor(Color.WHITE);
                bt_Stop.setEnabled(false);
                bt_Stop.setTextColor(Color.GRAY);
			}
			progress_detail.setText(text);
		}
		
	};

	//����ģʽ
	public static UploadFragment getInstance(){
		return uploadFragment;
	}

}
