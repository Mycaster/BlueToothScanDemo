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
    	View parentView = inflater.inflate(R.layout.scan, container, false);//���������ļ�  
        parentView = inflater.inflate(R.layout.about, container, false);
        String str = "�ó�������ɨ����Χ������Ϣ���ϴ���������������ɷֱ�����ɨ�輰�ϴ���ʱ���Ƶ�ʣ����ݸ�ʽΪ ��" +
        		"\nʱ��\n�豸����\nMac��ַ\n�ź�ǿ��\n���뷽ʽ��UTF��8\n���ӷ�ʽ��Socket\n\n\n\n" +
        		"Made By ����";
        abouttext= (TextView)parentView.findViewById(R.id.aboutText);
		abouttext.setText(str);
        return parentView;
    }

}
