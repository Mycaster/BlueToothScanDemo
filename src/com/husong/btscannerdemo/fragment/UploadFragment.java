package com.husong.btscannerdemo.fragment;

import com.husong.btscannerdemo.R;
import com.husong.btscannerdemo.controller.Tools;
import com.husong.btscannerdemo.controller.UploadService;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

public class UploadFragment extends Fragment{

	/*
	 * 控件
	 */
	private EditText ip_edit;
    private EditText port_edit;
    private TextView detail_tx,progress_detail;
    private Button settingBt;
    private Button bt_Stop;
	private Button bt_Send;
	private Button bt_upStart,bt_upEnd;
	private EditText uploadInterval;
	private TimePicker UploadTimepicker;
	private String TAG = "UploadFragment";
	private StringBuilder uploadInfo = new StringBuilder();
	
	private SharedPreferences MyPreferences;
	private SharedPreferences.Editor editor;
	
	private Activity main;
	private Intent i;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
    	MyPreferences = getActivity().getSharedPreferences("test",Context.MODE_MULTI_PROCESS);
        editor = MyPreferences.edit();
    	main = this.getActivity();
    	i = new Intent(main, UploadService.class);
    	
        Log.i("Activity Message ", main+" in Fragment");
        View rootView = inflater.inflate(R.layout.upload, container, false);//关联布局文件  
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
				uploadInfo.delete(0, uploadInfo.length());
				uploadInfo.append("上传进度:\n");
				progress_detail.setText(uploadInfo);
		        Log.i(TAG, MyPreferences.getString("ip",null));
		        Log.i(TAG, MyPreferences.getInt("port", 0)+"");
                //设置新Task的方式
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                main.startService(i);
                Log.i("UploadFragment", "Service is started");
                //bt_Send.s
                bt_Send.setEnabled(false);
                bt_Stop.setEnabled(true);
			}
		});
        
        bt_Stop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
                main.stopService(i);
                bt_Send.setEnabled(true);
                bt_Stop.setEnabled(false);
			}
		});
        
    	return rootView;
    }
}
