package com.husong.btscannerdemo.fragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

public class MenuActivity extends FragmentActivity implements View.OnClickListener{

    private ResideMenu resideMenu;
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
        
        FragmentCache.add(RegistFragment.getInstance());
        FragmentCache.add(UploadFragment.getInstance());
        FragmentCache.add(ScanFragment.getInstance());
        FragmentCache.add(AboutFragment.getInstance());
        
        setUpMenu();
        if( savedInstanceState == null ){
           // changeFragment(RegistFragment.getInstance());//����һ��Ӧ

            //����2��Ӧ
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, RegistFragment.getInstance(), "fragmentTag")
            .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit();
        }
        //InitialGloalData();
    }

    private void InitialGloalData() {
        /*
         * ��ʼ��ȫ�ֱ�����
         * ��SharedPreference�洢��ɨ�裬�ϴ����ݷ�����صĲ���
         * 
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
        itemScan 	 = new ResideMenuItem(this,R.drawable.icon_scan,	  "ɨ��");
        itemHome     = new ResideMenuItem(this, R.drawable.icon_regist,     "ע��");
        itemUpload   = new ResideMenuItem(this,R.drawable.icon_upload,	  "�ϴ�");
        itemAbout    = new ResideMenuItem(this,R.drawable.icon_about, "����");
        
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
            //Toast.makeText(mContext, "Menu is opened!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void closeMenu() {
            //Toast.makeText(mContext, "Menu is closed!", Toast.LENGTH_SHORT).show();
        }
    };

    //����һ�� ԭʼ��changeFragment�����ܱ���Fragment ״̬
/*  	private void changeFragment(Fragment targetFragment){
        resideMenu.clearIgnoredViewList();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment, targetFragment, "fragment")
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }*/
	
    
    //��������ʹ�û�����ʵ��Fragment��״̬����
     private void changeFragment(Fragment menuItem){
   		resideMenu.clearIgnoredViewList();
   		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
   		transaction.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 		Log.i("Traget Fragment Status", (menuItem==null)+"");
 		Fragment fragment = retrieveFromCache(menuItem);
 		transaction.replace(R.id.main_fragment, fragment,"fragmentTag");
 		transaction.commit();
 	}
     
 	private Fragment retrieveFromCache(Fragment menuItem) {
 		Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("fragmentTag");
 		//���õ�ǰ�µ�Fragment�滻�ɵ�Fragment
 		Iterator<Fragment> e = FragmentCache.iterator();    
 		while(e.hasNext()){    
 		  	Fragment element = e.next();  
 			if(currentFragment.getClass().equals(element.getClass())) {
 				e.remove();
 			}
 		}
 		FragmentCache.add(currentFragment);
 		//�ٽ�Ŀ��Fragment������
 		for (Fragment backFragment : FragmentCache){
 			if (null != backFragment
 					&& menuItem.getClass().equals(backFragment.getClass())) {
 				return backFragment;
 			}
 		}
 		return menuItem;
 	}
    
    
	//��������ʹ��hide() ��show() 
/*	private void changeFragment(Fragment targetFragment){
    	FragmentTransaction fac = getSupportFragmentManager().beginTransaction();
    	fac.hide(getSupportFragmentManager().findFragmentByTag("fagmentTag"));
    	fac.show(targetFragment);
    	fac.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
	}*/
	
    
    //�����ġ�
    /*private void changeFragment(Fragment targetFragment,int index){
    	FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction(); 
        for (int i = 0; i < 4; i++) { 
	        Fragment userFragment = fm.findFragmentByTag("fagmentTag"); 
	        Fragment fragment = null; 
	        if (userFragment != null){
	        	fragment = userFragment;
	        }else {
	        	fragment = FragmentCache.get(i); //mFragments�Ǵ洢���fragment instance��LIST 
	        }
	        if (i == index) {
		        if (!fragment.isAdded()) { 
		        	ft.add(R.id.main_fragment, fragment,TabTag(i));//������������Tag 
		        }
	        } else { 
		        if (fragment.isAdded()) { 
			        ft.remove(fragment); 
			        ft.addToBackStack(null); 
		        }
	        }
        }
        ft.commit();
    }*/
    
    
    
    
    
    
    
    
    
    
    
    
    
    // What good method is to access resideMenu
    public ResideMenu getResideMenu(){
        return resideMenu;
    }
}
