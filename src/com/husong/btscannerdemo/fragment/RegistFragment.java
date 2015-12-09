package com.husong.btscannerdemo.fragment;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
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
import com.husong.btscannerdemo.R;
import com.husong.btscannerdemo.bean.iBeacon;
import com.husong.btscannerdemo.controller.Tools;

public class RegistFragment extends Fragment {

	/*
	 * @see android.support.v4.app.Fragment#onDestroyView()
	 * 一旦切换出此Fragment ，则会调用onDestoryView,此时应取消广播的注册，以免和扫描界面的广播冲突(non-Javadoc)
	 */
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.i(TAG, "onDestroyView");
		//取消注册广播
  	    getActivity().unregisterReceiver(receiver);
	}
	
	private View RegistParentView;
    private ListView register_listview ;
    private String TAG = "HomeFragment";
    private Button regist_bt ;
    private Button setport_bt,connect_bt;
    private EditText regist_port;
    private TextView Progress_Regist;

    private StringBuilder detailInfo = new StringBuilder();
    private static PrintWriter out = null;
    private static Socket clientSocket= null; 
    private SharedPreferences config_sp;
    private SharedPreferences.Editor config_editor;
    private static final RegistFragment registFragment = new RegistFragment();
    
    //与蓝牙有关的
	private BluetoothAdapter registerScanAdapter;	
	private Map<String,iBeacon> registeResult;//扫描结果
    private ArrayList<HashMap<String, Object>> registerlistItems;//存放文字、图片信息
    private SimpleAdapter registerlistAdapter;//ListView适配器
	private BluetoothManager mBtManager;
	private static boolean isConnected = false;

	
	/*
	 * 每次切换至该页面时均会调用onCreateView()
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i("OnCreateView","OnCreateView()");
		//保存View的视图，保证Fragment切换时,View的状态可以恢复
		if (RegistParentView == null) {
	        RegistParentView = inflater.inflate(R.layout.regist, container, false);
		} else {
            ((ViewGroup)RegistParentView.getParent()).removeView(RegistParentView);
        }
		
		/*
		 * 控件初始化:
		 * setport_bt:设置端口的按钮
		 * connect_bt:连接服务器的按钮
		 * regist_bt: 注册的按钮
		 */
		setport_bt = (Button)RegistParentView.findViewById(R.id.setPortBt);
		connect_bt = (Button)RegistParentView.findViewById(R.id.ConnectServer);
        regist_bt = (Button)RegistParentView.findViewById(R.id.btn_open_menu);
		regist_port = (EditText)RegistParentView.findViewById(R.id.regist_port);
        register_listview = (ListView)RegistParentView.findViewById(R.id.register_list);
        Progress_Regist = (TextView)RegistParentView.findViewById(R.id.Progress_Regist);
        
        //
        config_sp = getActivity().getSharedPreferences("config",Context.MODE_MULTI_PROCESS);
        config_editor = config_sp.edit();
		registerlistItems = new ArrayList<HashMap<String, Object>>();
		registeResult = new HashMap<String,iBeacon>();
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
        
        setport_bt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(!regist_port.getText().toString().equals("")){
					int port =Integer.parseInt(regist_port.getText().toString());
					config_editor.putInt("RegistPort", port);
					config_editor.commit();
					regist_port.setHint("当前注册端口为："+port);
					regist_port.setText("");
				}
			}
		});
        
        connect_bt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				connect_bt.setTextColor(Color.GRAY);
				connect_bt.setEnabled(false);
				new Thread(new Runnable() {
					@Override
					public void run() {
						connect();
						if(isConnected){
							mHandler.sendEmptyMessage(0x123);
						}else {
							mHandler.sendEmptyMessage(0x124);
						}
					}
				}).start();
			}
		});
        

        regist_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	if(clientSocket==null){
            		Toast.makeText(getActivity(), "说好的先连接服务器呢", Toast.LENGTH_LONG).show();
            	}else{
            		if (!registerScanAdapter.isDiscovering())
    				{
                		regist_bt.setTextColor(Color.GRAY);
                		regist_bt.setText("正在注册");
                		regist_bt.setEnabled(false);
                		detailInfo.delete(0, detailInfo.length());
                    	detailInfo.append("注册进度: ");
                    	detailInfo.append("正在扫描—>");
                    	Progress_Regist.setText(detailInfo);
                    	registeResult.clear();
                    	registerlistItems.removeAll(registerlistItems);
                    	registerScanAdapter.startDiscovery();
    				}else {
    					Toast.makeText(getActivity(), "正在注册请稍后...", Toast.LENGTH_LONG).show();
    				}
            	}
            }
        });
        return RegistParentView;
    }
	
	protected void connect() {
	   	   try{ 
	   		    String ip = config_sp.getString("ip", null);
	   		    int port = config_sp.getInt("RegistPort", 0);
	   		    Log.i("Address: ",ip+":"+port);
	   		    mHandler.sendEmptyMessage(0x125);
	   		    clientSocket = new Socket(ip,port);
	   		    clientSocket.setKeepAlive(true);
				isConnected = true;
				out = new PrintWriter(new BufferedWriter(
				        new OutputStreamWriter(clientSocket.getOutputStream(),"UTF-8")),true);
				Log.i("Socket Connect", "连接成功");
	       }catch (UnknownHostException e){
	           e.printStackTrace();
				Log.i("Socket Connect", "连接失败1");
	       }catch (IOException e){
	           e.printStackTrace();
				Log.i("Socket Connect", "连接失败2");
	       }
	}
	
	//向服务器发送数据
	protected void send(String datas) throws SocketException {
        if(clientSocket.isConnected()&&!clientSocket.isClosed()){
        	if(!isServerClose(clientSocket)){
        		out.println(datas);
            	Log.i("send status", "数据已发送");
               	detailInfo.append("注册信息已发送"); 
            	Progress_Regist.setText(detailInfo);
        	}
        }else {
        	Progress_Regist.setText("检查服务器连接！");
        	connect_bt.setText("连接失败,请重新连接");
			connect_bt.setTextColor(Color.WHITE);
			connect_bt.setEnabled(true);
        }
    	regist_bt.setText("注册");
    	regist_bt.setTextColor(Color.WHITE);
		regist_bt.setEnabled(true);
	}
	
	//判断服务器连接状态
	public Boolean isServerClose(Socket socket){  
	   try{ 
		    socket.sendUrgentData(0xFF);//发送紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信  
		    return false;  
	   }catch (SocketException e){
			e.printStackTrace();
        	Progress_Regist.setText("检查服务器连接！");
        	connect_bt.setText("连接失败,请重新连接");
			connect_bt.setTextColor(Color.WHITE);
			connect_bt.setEnabled(true);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return true;
		}
	}
	
	//广播接受扫描到的蓝牙设备
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
				        mBeacon.setName(device.getName()); // 获取设备名称  
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
				detailInfo.append("扫描结束—>正在发送注册信息->");
            	Progress_Regist.setText(detailInfo);
            	StringBuilder datas = new StringBuilder("regist\n");
                for(String addr:registeResult.keySet()){
                	datas.append(registeResult.get(addr).getName()+"\n"+addr+"\n");
                }
                Log.i("regist data",datas.toString());
                datas.append("exit");
            	try {
					send(datas.toString());
				} catch (SocketException e) {
					e.printStackTrace();
				}
				DisPlayRegisterList();
			}
		}
	};
	//用于主线程的ＵＩ更新
	@SuppressLint("HandlerLeak") 
	private  Handler mHandler=new Handler(){
		public void handleMessage(Message msg) {
			if (msg.what == 0x123) {
				connect_bt.setText("服务器连接成功");
				connect_bt.setTextColor(Color.GRAY);
				connect_bt.setEnabled(false);
		        Log.i("Current Thread ID",""+Thread.currentThread().getId());
			}else if(msg.what==0x124){
				connect_bt.setText("连接失败,请重新连接");
				connect_bt.setTextColor(Color.WHITE);
				connect_bt.setEnabled(true);
			}else if(msg.what==0x125){
				connect_bt.setText("正在连接");
			}
		}
	};
	
	//将扫描结果用listView显示出来
	private void DisPlayRegisterList()
    {
        for(String addr:registeResult.keySet())
        {
            HashMap<String, Object> map = new HashMap<String, Object>();   
            map.put("time", Tools.getCurrentTime());
            map.put("name", registeResult.get(addr).getName()+"   ");
            map.put("address", addr);
            map.put("rssi","RSSI: "+registeResult.get(addr).getRSSI());
            map.put("ItemImage",R.drawable.bluetooth);   //图片   
            registerlistItems.add(map);
        }
        //生成适配器的Item和动态数组对应的元素   
        registerlistAdapter = new SimpleAdapter(
        		getActivity(),
        		registerlistItems,   // listItems数据源    
                R.layout.list_item,  //ListItem的XML布局实现  
                new String[] {"time","name","address","rssi","ItemImage"},     //动态数组与ImageItem对应的子项         
                new int[] {R.id.address, R.id.major,R.id.minor,R.id.rssi,R.id.ItemImage}//list_item.xml布局文件里面的一个ImageView的ID,一个TextView 的ID  
        );
        register_listview.setAdapter(registerlistAdapter);
    }
	
	//单例模式，获得RegistFragment 的实例
	public static RegistFragment getInstance(){
		return registFragment;
	}
}
