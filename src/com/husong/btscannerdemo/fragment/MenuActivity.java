package com.husong.btscannerdemo.fragment;
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
	
	//Activity 实例，单例模式
    public static MenuActivity getInstance() {
        return instance;
    }
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.i("Application Message", getApplication()+" in Activity");
        instance = this;
        setUpMenu();//页面初始化
        InitialGloalData();//全局参数初始化
        if( savedInstanceState == null ){//显示当前页面为注册页面
            changeFragment(RegistFragment.getInstance());
        }
    }

    
    
	private void InitialGloalData() {
        
         /* 初始化全局变量：
         * 用SharedPreference存储与扫描，上传数据服务相关的参数
         * 与上传有关参数:
    	 * address: ip地址
    	 * port: 端口号
    	 * startHour,startMin: 开始上传的时间
    	 * EndHour,EndMin: 结束上传时间
    	 * uploadInterval: 上传时间间隔
    	 * uploadCount: 上传次数
    	 * 
         * 与扫描有关参数:
         * startScanHour,startScanMin: 开始上传的时间
    	 * EndScanHour,EndScanMin: 结束上传时间
    	 * scanInterval: 扫描时间间隔
    	 * scanCount: 扫描次数
    	 */
		config_sp = this.getSharedPreferences("config",Context.MODE_MULTI_PROCESS);
		config_editor = config_sp.edit();
        if(!config_sp.getBoolean("isInitialed", false)){//只在第一次安装时初始化
        	config_editor.putBoolean("isInitialed", true);
        	config_editor.putString("ip", "192.168.1.181");
        	config_editor.putInt("port", 9999);
        	config_editor.putInt("RegistPort", 8888);
        	config_editor.putInt("StartUploadHour", 8);
        	config_editor.putInt("StartUploadMin", 30);
        	config_editor.putInt("EndUploadHour", 16);
        	config_editor.putInt("EndUploadMin", 30);
        	config_editor.putInt("UploadInterval", 8);
        	config_editor.putInt("UploadCount",75);
        	
        	config_editor.putInt("StartScanHour", 8);
        	config_editor.putInt("StartScanMin", 25);
        	config_editor.putInt("EndScanHour", 16);
        	config_editor.putInt("EndScanMin", 25);
        	config_editor.putInt("ScanInterval", 60);
        	config_editor.putInt("ScanCount",30);
        	config_editor.commit();	
        }
        
        /*
         * 保存各按钮的状态以及扫描上传进度等信息
         */
        status_sp = this.getSharedPreferences("status",Context.MODE_MULTI_PROCESS);
		status_editor = status_sp.edit();
    	status_editor.putBoolean("isUploading", false);
    	status_editor.putBoolean("isRegisting", false);
    	status_editor.putBoolean("isScanning", false);
    	status_editor.commit();	
	}

	//菜单栏的启动，初始化Fragment
	private void setUpMenu() {
        resideMenu = new ResideMenu(this);
        resideMenu.setUse3D(true);
        resideMenu.setBackground(R.drawable.menu_background);
        resideMenu.attachToActivity(this);
        resideMenu.setMenuListener(menuListener);
        //valid scale factor is between 0.0f and 1.0f. leftmenu'width is 150dip. 
        resideMenu.setScaleValue(0.6f);
        // create menu items;
        itemScan 	 = new ResideMenuItem(this,R.drawable.icon_scan,	  "扫描");
        itemHome     = new ResideMenuItem(this, R.drawable.icon_regist,     "注册");
        itemUpload   = new ResideMenuItem(this,R.drawable.icon_upload,	  "上传");
        itemAbout    = new ResideMenuItem(this,R.drawable.icon_about, "关于");
        
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

    
    //点击不同的按钮进入不同的Fragment
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

    //Fragment的切换
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
