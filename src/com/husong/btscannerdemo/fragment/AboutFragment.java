package com.husong.btscannerdemo.fragment;

import com.husong.btscannerdemo.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AboutFragment extends Fragment{
	
	private static final AboutFragment aboutFragment = new AboutFragment();
	public static AboutFragment getInstance(){
		return aboutFragment;
	}
	 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View parentView = inflater.inflate(R.layout.scan, container, false);//关联布局文件  
        parentView = inflater.inflate(R.layout.about, container, false);
        return parentView;
    }

}
