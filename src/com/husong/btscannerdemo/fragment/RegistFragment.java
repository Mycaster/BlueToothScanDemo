package com.husong.btscannerdemo.fragment;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.husong.btscannerdemo.R;
import com.husong.btscannerdemo.bean.iBeacon;
import com.husong.btscannerdemo.controller.Tools;
import com.special.ResideMenu.ResideMenu;

public class RegistFragment extends Fragment {

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		Log.i(TAG, "onDestroyView");
		//取消注册广播
  	    getActivity().unregisterReceiver(receiver);
	}

	private View parentView;
    private ResideMenu resideMenu;
    private ListView register_listview ;
    private String TAG = "HomeFragment";
    private Button regist_bt ;
    private StringBuilder detailInfo = new StringBuilder();
	private TextView Progress_Regist;
    
    private PrintWriter out = null;
    private Socket clientSocket= null; 
    private SharedPreferences mysp;
    
    //与蓝牙有关的
	private BluetoothAdapter registerScanAdapter;	
	private Map<String,iBeacon> registeResult;
    private ArrayList<HashMap<String, Object>> registerlistItems;    //存放文字、图片信息
    private SimpleAdapter registerlistAdapter;         		 //适配器
	private BluetoothManager mBtManager;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.regist, container, false);
        MenuActivity parentActivity = (MenuActivity) getActivity();
        resideMenu = parentActivity.getResideMenu();
        register_listview = (ListView)parentView.findViewById(R.id.register_list);
        Progress_Regist = (TextView)parentView.findViewById(R.id.Progress_Regist);
        mysp = getActivity().getSharedPreferences("test",Context.MODE_MULTI_PROCESS);
		registerlistItems = new ArrayList<HashMap<String, Object>>();
		registeResult = new HashMap<String,iBeacon>();
		detailInfo.append("注册进度: ");
		Progress_Regist.setText(detailInfo);
  	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
  	    	mBtManager = (BluetoothManager)getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
  	    	registerScanAdapter = mBtManager.getAdapter();
  	    }
  	    if(!registerScanAdapter.isEnabled()){	
	  		Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	  		startActivityForResult(enableIntent, 1);
	  	}
  	    
  	    //注册广播
  	    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
  		getActivity().registerReceiver(receiver, filter);
  		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(receiver, filter);
        regist_bt = (Button)parentView.findViewById(R.id.btn_open_menu);
        regist_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            	if (!registerScanAdapter.isDiscovering())
				{
            		regist_bt.setTextColor(Color.GRAY);
            		regist_bt.setText("正在注册");
            		//regist_bt.setEnabled(false);
            		detailInfo.delete(0, detailInfo.length());
                	detailInfo.append("注册进度: ");
                	detailInfo.append("开始扫描—>正在扫描—>");
                	Progress_Regist.setText(detailInfo);
                	registeResult.clear();
                	registerlistItems.removeAll(registerlistItems);
                	registerScanAdapter.startDiscovery();
				}else {
					Toast.makeText(getActivity(), "正在注册请稍后...", Toast.LENGTH_LONG).show();
				}
            }
        });
        return parentView;
    }

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
			        if (!registeResult.containsKey(address)){
						iBeacon mBeacon = new iBeacon();
						mBeacon.setTime(Tools.getCurrentTime());
				        mBeacon.setName(device.getName());         // 获取设备名称  
				        mBeacon.setAddress(address);		
				        mBeacon.setRSSI(rssi);
				        registeResult.put(address, mBeacon);
			            System.out.println("device is saved in mapscanResult");
			        }
				}
			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
			{
				System.out.println("discovery is finished");
				detailInfo.append("扫描结束—>正在连接服务器—>");
            	Progress_Regist.setText(detailInfo);
				sendRegistMessage();
				DisPlayRegisterList();
			}
		}
	};
	
	private void DisPlayRegisterList()
    {
		 //register_listview.re
		Log.i(TAG, "Enter DisPlayData()");
        for(String addr:registeResult.keySet())
        {
            HashMap<String, Object> map = new HashMap<String, Object>();   
            map.put("time", Tools.getCurrentTime());
            map.put("name", registeResult.get(addr).getName()+"   ");
            map.put("address", addr);
            map.put("rssi","RSSI: "+registeResult.get(addr).getRSSI());
            map.put("ItemImage",R.drawable.bluetooth);   //图片   
            registerlistItems.add(map);
            Log.i(TAG, map.get("name")+"");
        }
        //生成适配器的Item和动态数组对应的元素   
        registerlistAdapter = new SimpleAdapter(
        		getActivity(),
        		registerlistItems,   // listItems数据源    
                R.layout.list_item,  //ListItem的XML布局实现  
                new String[] {"time","name","address","rssi","ItemImage"},     //动态数组与ImageItem对应的子项         
                new int[] {R.id.address, R.id.major,R.id.minor,R.id.rssi,R.id.ItemImage}//list_item.xml布局文件里面的一个ImageView的ID,一个TextView 的ID  
        );
        Log.i(TAG,"registerlistAdapter 数据已装载");
        register_listview.setAdapter(registerlistAdapter);
    }
	
	protected void sendRegistMessage() {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(connect()){
					mHandler.sendEmptyMessage(0x123);
				}else {
					mHandler.sendEmptyMessage(0x124);
				}
			}
		}).start();
	}

	private Handler mHandler=new Handler(){
		public void handleMessage(Message msg) {
			if (msg.what == 0x123) {
				detailInfo.append("服务器连接成功—>");
            	Progress_Regist.setText(detailInfo);
				send();
		        Log.i("Current Thread ID",""+Thread.currentThread().getId());
			}else if(msg.what==0x124){
				detailInfo.append("服务器连接失败");
            	Progress_Regist.setText(detailInfo);
            	regist_bt.setTextColor(Color.WHITE);
        		regist_bt.setEnabled(true);
			}
		}
	};
	
	protected void send() {
		// TODO Auto-generated method stub
        StringBuilder datas = new StringBuilder();
        for(String addr:registeResult.keySet()){
        	datas.append(registeResult.get(addr).getTime()+"\n");
        	datas.append(registeResult.get(addr).getName()+":    "+addr+"\n");
        	datas.append("RSSI: "+registeResult.get(addr).getRSSI()+"\n");
        }
        Log.i("regist data",datas.toString());
       	out.println(datas);
       	Log.i("send status", "数据已发送");
       	detailInfo.append("注册信息已发送—>");
    	Progress_Regist.setText(detailInfo);
       	//regist_bt.setText("注册信息已发送");
    	disconnect();
	}
	
	protected void disconnect() {
      	try {
			clientSocket.shutdownOutput();
			clientSocket.close();
			detailInfo.append("服务器连接已关闭");
        	Progress_Regist.setText(detailInfo);
        	regist_bt.setTextColor(Color.WHITE);
    		regist_bt.setEnabled(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected boolean connect() {
		// TODO Auto-generated method stub
   	   try{ 
   		    String ip = mysp.getString("ip", null);
   		    int port = mysp.getInt("port", 0);
   		    Log.i("Address: ",ip);
   		    Log.i("Port: ",port+"");
   		    clientSocket = new Socket(ip,port);
			Log.i("Socket Connect", "连接成功");
			//获得输入流
	   		out = new PrintWriter(new BufferedWriter(  
                   new OutputStreamWriter(clientSocket.getOutputStream(),"UTF-8")),true); 
	   		Log.i("Socket Connect", "获得输出流句柄");
	   		return true;
       }catch (UnknownHostException e){
           e.printStackTrace();
			Log.i("Socket Connect", "连接失败1");
			return false;
       }catch (IOException e){
           e.printStackTrace();
			Log.i("Socket Connect", "连接失败2");
			return false;
       }
	}
}
