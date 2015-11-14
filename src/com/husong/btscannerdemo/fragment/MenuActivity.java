package com.husong.btscannerdemo.fragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.husong.btscannerdemo.R;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

public class MenuActivity extends FragmentActivity implements View.OnClickListener{

    private ResideMenu resideMenu;
    private MenuActivity mContext;
    private ResideMenuItem itemHome;
    private ResideMenuItem itemScan;
    private ResideMenuItem itemUpload;
	private ResideMenuItem itemAbout;
    private SharedPreferences MyPreferences;
    private SharedPreferences.Editor editor;    

    private List<Fragment> FragmentCache = new ArrayList<Fragment>();
    
	private static MenuActivity instance ;
	
    public static MenuActivity getInstance() {
        return instance;
    }
    
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.i("Application Message", getApplication()+" in Activity");
        instance = this;
        
        FragmentCache.add(new RegistFragment());
        FragmentCache.add(new UploadFragment());
        FragmentCache.add(new ScanFragment());
        FragmentCache.add(new AboutFragment());
        
        setUpMenu();
        if( savedInstanceState == null ){
        	getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, new RegistFragment(), "fragmentTag")
            .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit();
        }
        //InitialGloalData();
    }

    private void InitialGloalData() {
        /*
         * 初始化全局变量：
         * 用SharedPreference存储与扫描，上传数据服务相关的参数
         * 
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
    	MyPreferences = this.getSharedPreferences("test",Context.MODE_MULTI_PROCESS);
        editor = MyPreferences.edit();
    	editor.putString("ip", "192.168.1.181");
    	editor.putInt("port", 60000);
    	editor.putInt("StartUploadHour", 7);
    	editor.putInt("StartUploadMin", 30);
    	editor.putInt("EndUploadHour", 17);
    	editor.putInt("EndUploadMin", 30);
    	editor.putInt("UploadInterval", 8);
    	editor.putInt("UploadCount",75);
    	
    	editor.putInt("StartScanHour", 7);
    	editor.putInt("StartScanMin", 25);
    	editor.putInt("EndScanHour", 17);
    	editor.putInt("EndScanMin", 25);
    	editor.putInt("ScanInterval", 20);
    	editor.putInt("ScanCount",30);
    	editor.commit();
	}

	private void setUpMenu() {
        // attach to current activity;
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
        resideMenu.addMenuItem(itemScan,ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemUpload,ResideMenu.DIRECTION_LEFT);
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

    @Override
    public void onClick(View view) {
        if (view == itemHome){
            changeFragment(new RegistFragment());
        }else if (view==itemScan){
        	changeFragment(new ScanFragment());
        }else if(view==itemUpload){
        	changeFragment(new UploadFragment());
        }else if(view==itemAbout){
        	changeFragment(new AboutFragment());
        }
        resideMenu.closeMenu();
    }

    private ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener() {
        @Override
        public void openMenu() {
            //Toast.makeText(mContext, "Menu is opened!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void closeMenu() {
            //Toast.makeText(mContext, "Menu is closed!", Toast.LENGTH_SHORT).show();
        }
    };

    private void changeFragment(Fragment menuItem){
  		resideMenu.clearIgnoredViewList();
  		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
  		transaction.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		Log.i("Traget Fragment Status", (menuItem==null)+"");
		Fragment fragment = retrieveFromCache(menuItem);
		//transaction.addToBackStack(null);
		transaction.replace(R.id.main_fragment, fragment,"fragmentTag");
		transaction.commit();
	}
	private Fragment retrieveFromCache(Fragment menuItem) {
		Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("fragmentTag");
		Iterator<Fragment> e = FragmentCache.iterator();    
		while(e.hasNext()){    
		  	Fragment element = e.next();  
			if(currentFragment.getClass().equals(element.getClass())) {
				e.remove();
			}
		}
		FragmentCache.add(currentFragment);
		//先用当前新的Fragment替换旧的Fragment
//		for (Fragment oldFragment : FragmentCache){
//			if (currentFragment.getClass().equals(oldFragment.getClass())) {
//				FragmentCache.remove(oldFragment);
//				FragmentCache.add(currentFragment);
//			}
//		}
		for (Fragment backFragment : FragmentCache){
			if (null != backFragment
					&& menuItem.getClass().equals(backFragment.getClass())) {
				return backFragment;
			}
		}
		return menuItem;
	}
    

 // 原来的changeFragment  
/*  	private void changeFragment(Fragment targetFragment){
        resideMenu.clearIgnoredViewList();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment, targetFragment, "fragment")
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }*/
    // What good method is to access resideMenu
    public ResideMenu getResideMenu(){
        return resideMenu;
    }
}
