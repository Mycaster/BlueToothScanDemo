package com.husong.btscannerdemo.fragment;
import com.husong.btscannerdemo.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends Fragment{
	private TextView abouttext;
	private static final AboutFragment aboutFragment = new AboutFragment();
	public static AboutFragment getInstance(){
		return aboutFragment;
	}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View parentView = inflater.inflate(R.layout.scan, container, false);//关联布局文件  
        parentView = inflater.inflate(R.layout.about, container, false);
        String str = "该程序用于扫描周围蓝牙信息并上传至服务器，具体可分别设置扫描及上传的时间和频率，数据格式为 ：" +
        		"\n时间\n设备名称\nMac地址\n信号强度\n编码方式：UTF—8\n连接方式：Socket\n\n\n\n" +
        		"Made By 胡松";
        abouttext= (TextView)parentView.findViewById(R.id.aboutText);
		abouttext.setText(str);
        return parentView;
    }

}
