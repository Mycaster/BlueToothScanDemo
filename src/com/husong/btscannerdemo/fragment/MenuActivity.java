package com.husong.btscannerdemo.fragment;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.husong.btscannerdemo.R;
import com.husong.btscannerdemo.resideMenu.ResideMenu;
import com.husong.btscannerdemo.resideMenu.ResideMenuItem;

public class MenuActivity extends FragmentActivity implements View.OnClickListener{

    private ResideMenu resideMenu;
    private ResideMenuItem itemHome;
    private ResideMenuItem itemScan;
    private ResideMenuItem itemUpload;
	private ResideMenuItem itemAbout;
	private static MenuActivity instance ;
    private static SharedPreferences config_sp,status_sp;
	private static SharedPreferences.Editor config_editor,status_editor; 
	
	//Activity ʵ��������ģʽ
    public static MenuActivity getInstance() {
        return instance;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.i("Application Message", getApplication()+" in Activity");
        instance = this;
        setUpMenu();//ҳ���ʼ��
        InitialGloalData();//ȫ�ֲ�����ʼ��
        if( savedInstanceState == null ){//��ʾ��ǰҳ��Ϊע��ҳ��
            changeFragment(RegistFragment.getInstance());
        }
    }

    
    
	private void InitialGloalData() {
        
         /* ��ʼ��ȫ�ֱ�����
         * ��SharedPreference�洢��ɨ�裬�ϴ����ݷ�����صĲ���
         * ���ϴ��йز���:
    	 * address: ip��ַ
    	 * port: �˿ں�
    	 * startHour,startMin: ��ʼ�ϴ���ʱ��
    	 * EndHour,EndMin: �����ϴ�ʱ��
    	 * uploadInterval: �ϴ�ʱ����
    	 * uploadCount: �ϴ�����
    	 * 
         * ��ɨ���йز���:
         * startScanHour,startScanMin: ��ʼ�ϴ���ʱ��
    	 * EndScanHour,EndScanMin: �����ϴ�ʱ��
    	 * scanInterval: ɨ��ʱ����
    	 * scanCount: ɨ�����
    	 */
		config_sp = this.getSharedPreferences("config",Context.MODE_MULTI_PROCESS);
		config_editor = config_sp.edit();
        if(!config_sp.getBoolean("isInitialed", false)){//ֻ�ڵ�һ�ΰ�װʱ��ʼ��
        	config_editor.putBoolean("isInitialed", true);
        	config_editor.putString("ip", "192.168.1.181");
        	config_editor.putInt("port", 9999);
        	config_editor.putInt("RegistPort", 8888);
        	//��Dateת����long �洢
        	Date date = new Date();
        	date.setHours(8);
        	date.setMinutes(30);
        	config_editor.putLong("StartUploadTime", date.getTime());
        	config_editor.putLong("nextUploadTime", date.getTime());
        	date.setHours(16);
        	config_editor.putLong("EndUploadTime", date.getTime());
        	config_editor.putInt("UploadInterval", 8);
        	config_editor.putInt("ScanFreq", 1);
        	config_editor.putInt("UploadCount",75);
        	
        	date.setHours(8);
        	date.setMinutes(25);
        	config_editor.putLong("StartScanTime", date.getTime());
        	config_editor.putLong("nextScanTime", date.getTime());
        	date.setHours(16);
        	config_editor.putLong("EndScanTime", date.getTime());
        	config_editor.putInt("ScanInterval", 60);
        	config_editor.putInt("ScanCount",30);
        	config_editor.commit();
        }
        
        /*
         * �������ť��״̬�Լ�ɨ���ϴ����ȵ���Ϣ
         */
        status_sp = this.getSharedPreferences("status",Context.MODE_MULTI_PROCESS);
		status_editor = status_sp.edit();
    	status_editor.putBoolean("isUploading", false);
    	status_editor.putBoolean("isRegisting", false);
    	status_editor.putBoolean("isScanning", false);
    	status_editor.commit();	
	}

	//�˵�������������ʼ��Fragment
	private void setUpMenu() {
        resideMenu = new ResideMenu(this);
        resideMenu.setUse3D(true);
        resideMenu.setBackground(R.drawable.menu_background);
        resideMenu.attachToActivity(this);
        resideMenu.setMenuListener(menuListener);
        //valid scale factor is between 0.0f and 1.0f. leftmenu'width is 150dip. 
        resideMenu.setScaleValue(0.6f);
        // create menu items;
        itemScan 	 = new ResideMenuItem(this,R.drawable.icon_scan,	  "ɨ��");
        itemHome     = new ResideMenuItem(this, R.drawable.icon_regist,     "ע��");
        itemUpload   = new ResideMenuItem(this,R.drawable.icon_upload,	  "�ϴ�");
        itemAbout    = new ResideMenuItem(this,R.drawable.icon_about, "����");
        
        itemScan.setOnClickListener(this);
        itemHome.setOnClickListener(this);
        itemUpload.setOnClickListener(this);
        itemAbout.setOnClickListener(this);

        resideMenu.addMenuItem(itemHome, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemUpload,ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemScan,ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemAbout,ResideMenu.DIRECTION_LEFT);
    
        
        // You can disable a direction by setting ->
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

        findViewById(R.id.title_bar_left_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

    
    //�����ͬ�İ�ť���벻ͬ��Fragment
    @Override
    public void onClick(View view) {
        if (view == itemHome){
            changeFragment(RegistFragment.getInstance());
        }else if (view==itemScan){
        	changeFragment(ScanFragment.getInstance());
        }else if(view==itemUpload){
        	changeFragment(UploadFragment.getInstance());
        }else if(view==itemAbout){
        	changeFragment(AboutFragment.getInstance());
        }
        resideMenu.closeMenu();
    }

    private ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener() {
        @Override
        public void openMenu() {
        }
        @Override
        public void closeMenu() {
        }
    };

    //Fragment���л�
  	private void changeFragment(Fragment targetFragment){
        resideMenu.clearIgnoredViewList();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment, targetFragment, "fragment")
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }
  	
    // What good method is to access resideMenu
    public ResideMenu getResideMenu(){
        return resideMenu;
    }
}
