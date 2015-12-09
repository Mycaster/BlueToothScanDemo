package com.husong.btscannerdemo.fragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import com.husong.btscannerdemo.R;
import com.husong.btscannerdemo.bean.iBeacon;
import com.husong.btscannerdemo.controller.Tools;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ScanFragment extends Fragment
{
	private Button bt_scan,bt_set,bt_stopscan;
	private Button bt_setStartBt,bt_setEndBt;
	private int count=1;
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		getActivity().unregisterReceiver(receiver);
	}
	private View ScanrootView;
	private EditText et_scanInterval;//输入的扫描间隔
	private TextView scaninfo;
    private ArrayList<HashMap<String, Object>> listItems;    //存放文字、图片信息
    private SimpleAdapter listItemAdapter;         		 //适配器
    private ListView listview ;
    //与蓝牙有关的
	private BluetoothManager mBtManager;
	private BluetoothAdapter mBtAdapter;	
	private Map<String,iBeacon> mapScanResult;
	//定时器有关
	private Timer timer;
	private TimerTask task ;
	private boolean isTimerCancled =true;
    private SharedPreferences config_sp;
    private SharedPreferences.Editor editor;
    
    private boolean isButtonStop = false;
    
    private static int ScanCount2 ;
    
    private static final ScanFragment scanFragment = new ScanFragment();
    private static final long PERIOD_DAY = 24 * 60 * 60 * 1000;
    
    public static ScanFragment getInstance(){
    	return scanFragment;
    }
	
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2) 
    @Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
		 if (ScanrootView == null) {
			 ScanrootView = inflater.inflate(R.layout.scan, container, false);//关联布局文件
		 } else {
	         ((ViewGroup)ScanrootView.getParent()).removeView(ScanrootView);
	     }
		 config_sp = this.getActivity().getSharedPreferences("config",Context.MODE_MULTI_PROCESS);
        editor = config_sp.edit();
    	
        bt_scan = (Button)ScanrootView.findViewById(R.id.scan_bt);
        bt_stopscan = (Button)ScanrootView.findViewById(R.id.stopscan_bt);
        bt_set = (Button)ScanrootView.findViewById(R.id.setBt);
    	bt_setStartBt = (Button)ScanrootView.findViewById(R.id.setStarttimeBt);
    	bt_setEndBt = (Button)ScanrootView.findViewById(R.id.setEndtimeBt);
    	et_scanInterval = (EditText)ScanrootView.findViewById(R.id.et_scanInterval);
        listview = (ListView)ScanrootView.findViewById(R.id.list);
        scaninfo  = (TextView)ScanrootView.findViewById(R.id.scan_info);
        Tools.updatescanDetail(scaninfo);
        listItems = new ArrayList<HashMap<String, Object>>();
		mapScanResult = new HashMap<String,iBeacon>();
		 // 设备SDK版本大于17（Build.VERSION_CODES.JELLY_BEAN_MR1）才支持BLE 4.0 
  	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
  	    	mBtManager = (BluetoothManager)getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
  	    	mBtAdapter = mBtManager.getAdapter();
  	    }
  	    mBtAdapter = BluetoothAdapter.getDefaultAdapter();
  	    if(!mBtAdapter.isEnabled()){	
	  		Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	  		startActivityForResult(enableIntent, 1);
	  	}
  	    
  	    //注册广播
  	    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
  		getActivity().registerReceiver(receiver, filter);
  		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(receiver, filter);
  	    
		bt_setStartBt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Tools.dateTimePicKDialog("设置起始时间",scaninfo);
			}
		});
		
		bt_setEndBt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Tools.dateTimePicKDialog("设置结束时间",scaninfo);
				Log.i("", "对话框结束");
			}
		});

		bt_set.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(!et_scanInterval.getText().toString().equals("")){
					int scanInterval =Integer.parseInt(et_scanInterval.getText().toString());
					editor.putInt("ScanInterval", scanInterval);
					editor.commit();
					et_scanInterval.setText("");
					Tools.updatescanDetail(scaninfo);
				}
			}
		});
		
		bt_scan.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				Date scanDate = new Date();
			   	scanDate.setHours(config_sp.getInt("StartScanHour", 0));
			   	scanDate.setMinutes(config_sp.getInt("StartScanMin", 0));
			    Date EndScanDate =new Date();
			    EndScanDate.setHours(config_sp.getInt("EndScanHour", 0));
			    EndScanDate.setMinutes(config_sp.getInt("EndScanMin", 0));
			    if(!EndScanDate.after(scanDate)){//结束时间小于开始时间
					Toast.makeText(getActivity(), "结束时间请大于开始时间！", Toast.LENGTH_LONG).show();
				} else if(!mBtAdapter.isDiscovering()&&isTimerCancled){
					
					bt_scan.setText("准备扫描...");
					bt_scan.setEnabled(false);
					bt_scan.setTextColor(Color.BLACK);
					bt_stopscan.setEnabled(true);
					bt_stopscan.setTextColor(Color.WHITE);
					isButtonStop = false;
					isTimerCancled = false;
				   	timer = new Timer(true);
				   	task = taskGenerator();
					if(!scanDate.after(new Date())){//如果在设定的起始时间之后，则立即执行
						scanDate = new Date();
						int TotalTime = Tools.calculateTime(scanDate.getHours(),scanDate.getMinutes(),config_sp.getInt("EndScanHour", 0),config_sp.getInt("EndScanMin", 0));
						ScanCount2 = TotalTime*60/config_sp.getInt("ScanInterval", 0)+1;
					}
					timer.schedule(task, scanDate, config_sp.getInt("ScanInterval", 0)*1000); //定时执行执行，30s执行一次
					
				}
			}
		});
		
		bt_stopscan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				timer.cancel();
				isTimerCancled = true;
				isButtonStop = true;
				mBtAdapter.cancelDiscovery();
				count=1;//重新置1
				bt_scan.setText("开始扫描");
				bt_scan.setEnabled(true);
				bt_scan.setTextColor(Color.WHITE);
				bt_stopscan.setEnabled(false);
				bt_stopscan.setTextColor(Color.BLACK);
			}
		});
    	return ScanrootView;
    }    
    public TimerTask taskGenerator(){
    	return new TimerTask(){  
			 public void run() {  //另开的线程，不在UI线程里,所以不能显示数据
				 if(count <= ScanCount2){
					Log.i("Thread Id:",Thread.currentThread().getId()+"");
					Log.i("scan ", "第"+count+"次：开始扫描");
					if (mBtAdapter.isDiscovering())
						mBtAdapter.cancelDiscovery();
					mBtAdapter.startDiscovery();
					Log.i("mBtAdapter.isDiscovering():",mBtAdapter.isDiscovering()+"");
					Log.i("Message:0x123","discovery 开始");
					++count;
					mHandler.sendEmptyMessage(0x123);
				 }else {
					 mHandler.sendEmptyMessage(0x124);
					 task.cancel();//将上一次的任务和定时器取消
					 timer.cancel();
					 isTimerCancled=true;
					 count=1;
					 timer = new Timer();
					 task = taskGenerator();
					 Date scanDate = new Date();
					 scanDate.setDate(scanDate.getDay()+1);
				   	 scanDate.setHours(config_sp.getInt("StartScanHour", 0));
				   	 scanDate.setMinutes(config_sp.getInt("StartScanMin", 0));
					 timer.schedule(task, scanDate, config_sp.getInt("ScanInterval", 0)*1000); //定时执行执行，30s执行一次
					 Log.i("scan","timer is cancled");
				 }
			}
    	};
    }
    
	@SuppressLint("HandlerLeak") 
	private Handler mHandler=new Handler(){
		public void handleMessage(Message msg) {
			if (msg.what == 0x123) {
				bt_scan.setText("第"+(count-1)+"次扫描进行中");
			}if(msg.what ==0x124){
				bt_scan.setText("开始扫描");
				bt_scan.setEnabled(true);
				bt_scan.setTextColor(Color.WHITE);
				bt_stopscan.setEnabled(false);
				bt_stopscan.setTextColor(Color.BLACK);
			}
		}
	};
	private final BroadcastReceiver receiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action))
			{
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				System.out.println("reveive a device"+device.getName());
				short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
				if (device.getBondState() != BluetoothDevice.BOND_BONDED)
				{
					String address = device.getAddress();   // 获取Mac地址
			        if (!mapScanResult.containsKey(address)){
						iBeacon mBeacon = new iBeacon();
						mBeacon.setTime(Tools.getCurrentTime());
				        mBeacon.setName(device.getName());         // 获取设备名称  
				        mBeacon.setAddress(address);		
				        mBeacon.setRSSI(rssi);
			            mapScanResult.put(address, mBeacon);
			            System.out.println("device is saved in mapscanResult");
			        }
				}
			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
			{
				if(!isButtonStop){
					DisPlayData();
			        writeData();
					bt_scan.setText("第"+(count-1)+"次扫描结束");	
				}
			}
		}
	};
    
	private void DisPlayData()
    {
        for(String addr:mapScanResult.keySet())
        {
            HashMap<String, Object> map = new HashMap<String, Object>();   
            map.put("time", mapScanResult.get(addr).getTime());
            map.put("name", mapScanResult.get(addr).getName()+"   ");
            map.put("address", addr);
            map.put("rssi","RSSI: "+mapScanResult.get(addr).getRSSI());
            map.put("ItemImage",R.drawable.bluetooth);   //图片   
            listItems.add(map);
        }
        //生成适配器的Item和动态数组对应的元素   
        listItemAdapter = new SimpleAdapter(
        		getActivity(),
        		listItems,   // listItems数据源    
                R.layout.list_item,  //ListItem的XML布局实现  
                new String[] {"time","name","address","rssi","ItemImage"},     //动态数组与ImageItem对应的子项         
                new int[] {R.id.address, R.id.major,R.id.minor,R.id.rssi,R.id.ItemImage}//list_item.xml布局文件里面的一个ImageView的ID,一个TextView 的ID  
        );
        listview.setAdapter(listItemAdapter);
        System.out.println("数据已显示");
    }
    
    private void writeData(){
        StringBuilder datas = new StringBuilder();
        for(String addr:mapScanResult.keySet()){
        	datas.append(mapScanResult.get(addr).getTime()+"\n");
        	datas.append(addr+"\n");
        	datas.append(mapScanResult.get(addr).getRSSI()+"\n");
        }
        Tools.writeToFile("blueToothScan_data.txt", datas.toString());
        Tools.writeToFile("totalData.txt", datas.toString());
        mapScanResult.clear();
    }
}
