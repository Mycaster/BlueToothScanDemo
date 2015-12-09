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
	 * һ���л�����Fragment ��������onDestoryView,��ʱӦȡ���㲥��ע�ᣬ�����ɨ�����Ĺ㲥��ͻ(non-Javadoc)
	 */
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.i(TAG, "onDestroyView");
		//ȡ��ע��㲥
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
    
    //�������йص�
	private BluetoothAdapter registerScanAdapter;	
	private Map<String,iBeacon> registeResult;//ɨ����
    private ArrayList<HashMap<String, Object>> registerlistItems;//������֡�ͼƬ��Ϣ
    private SimpleAdapter registerlistAdapter;//ListView������
	private BluetoothManager mBtManager;
	private static boolean isConnected = false;

	
	/*
	 * ÿ���л�����ҳ��ʱ�������onCreateView()
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i("OnCreateView","OnCreateView()");
		//����View����ͼ����֤Fragment�л�ʱ,View��״̬���Իָ�
		if (RegistParentView == null) {
	        RegistParentView = inflater.inflate(R.layout.regist, container, false);
		} else {
            ((ViewGroup)RegistParentView.getParent()).removeView(RegistParentView);
        }
		
		/*
		 * �ؼ���ʼ��:
		 * setport_bt:���ö˿ڵİ�ť
		 * connect_bt:���ӷ������İ�ť
		 * regist_bt: ע��İ�ť
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
  	    //ע��㲥
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
					regist_port.setHint("��ǰע��˿�Ϊ��"+port);
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
            		Toast.makeText(getActivity(), "˵�õ������ӷ�������", Toast.LENGTH_LONG).show();
            	}else{
            		if (!registerScanAdapter.isDiscovering())
    				{
                		regist_bt.setTextColor(Color.GRAY);
                		regist_bt.setText("����ע��");
                		regist_bt.setEnabled(false);
                		detailInfo.delete(0, detailInfo.length());
                    	detailInfo.append("ע�����: ");
                    	detailInfo.append("����ɨ�衪>");
                    	Progress_Regist.setText(detailInfo);
                    	registeResult.clear();
                    	registerlistItems.removeAll(registerlistItems);
                    	registerScanAdapter.startDiscovery();
    				}else {
    					Toast.makeText(getActivity(), "����ע�����Ժ�...", Toast.LENGTH_LONG).show();
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
				Log.i("Socket Connect", "���ӳɹ�");
	       }catch (UnknownHostException e){
	           e.printStackTrace();
				Log.i("Socket Connect", "����ʧ��1");
	       }catch (IOException e){
	           e.printStackTrace();
				Log.i("Socket Connect", "����ʧ��2");
	       }
	}
	
	//���������������
	protected void send(String datas) throws SocketException {
        if(clientSocket.isConnected()&&!clientSocket.isClosed()){
        	if(!isServerClose(clientSocket)){
        		out.println(datas);
            	Log.i("send status", "�����ѷ���");
               	detailInfo.append("ע����Ϣ�ѷ���"); 
            	Progress_Regist.setText(detailInfo);
        	}
        }else {
        	Progress_Regist.setText("�����������ӣ�");
        	connect_bt.setText("����ʧ��,����������");
			connect_bt.setTextColor(Color.WHITE);
			connect_bt.setEnabled(true);
        }
    	regist_bt.setText("ע��");
    	regist_bt.setTextColor(Color.WHITE);
		regist_bt.setEnabled(true);
	}
	
	//�жϷ���������״̬
	public Boolean isServerClose(Socket socket){  
	   try{ 
		    socket.sendUrgentData(0xFF);//���ͽ������ݣ�Ĭ������£���������û�п����������ݴ�����Ӱ������ͨ��  
		    return false;  
	   }catch (SocketException e){
			e.printStackTrace();
        	Progress_Regist.setText("�����������ӣ�");
        	connect_bt.setText("����ʧ��,����������");
			connect_bt.setTextColor(Color.WHITE);
			connect_bt.setEnabled(true);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return true;
		}
	}
	
	//�㲥����ɨ�赽�������豸
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
					String address = device.getAddress();   // ��ȡMac��ַ
			        if (!registeResult.containsKey(address)){
						iBeacon mBeacon = new iBeacon();
						mBeacon.setTime(Tools.getCurrentTime());
				        mBeacon.setName(device.getName()); // ��ȡ�豸����  
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
				detailInfo.append("ɨ�������>���ڷ���ע����Ϣ->");
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
	//�������̵߳ģգɸ���
	@SuppressLint("HandlerLeak") 
	private  Handler mHandler=new Handler(){
		public void handleMessage(Message msg) {
			if (msg.what == 0x123) {
				connect_bt.setText("���������ӳɹ�");
				connect_bt.setTextColor(Color.GRAY);
				connect_bt.setEnabled(false);
		        Log.i("Current Thread ID",""+Thread.currentThread().getId());
			}else if(msg.what==0x124){
				connect_bt.setText("����ʧ��,����������");
				connect_bt.setTextColor(Color.WHITE);
				connect_bt.setEnabled(true);
			}else if(msg.what==0x125){
				connect_bt.setText("��������");
			}
		}
	};
	
	//��ɨ������listView��ʾ����
	private void DisPlayRegisterList()
    {
        for(String addr:registeResult.keySet())
        {
            HashMap<String, Object> map = new HashMap<String, Object>();   
            map.put("time", Tools.getCurrentTime());
            map.put("name", registeResult.get(addr).getName()+"   ");
            map.put("address", addr);
            map.put("rssi","RSSI: "+registeResult.get(addr).getRSSI());
            map.put("ItemImage",R.drawable.bluetooth);   //ͼƬ   
            registerlistItems.add(map);
        }
        //������������Item�Ͷ�̬�����Ӧ��Ԫ��   
        registerlistAdapter = new SimpleAdapter(
        		getActivity(),
        		registerlistItems,   // listItems����Դ    
                R.layout.list_item,  //ListItem��XML����ʵ��  
                new String[] {"time","name","address","rssi","ItemImage"},     //��̬������ImageItem��Ӧ������         
                new int[] {R.id.address, R.id.major,R.id.minor,R.id.rssi,R.id.ItemImage}//list_item.xml�����ļ������һ��ImageView��ID,һ��TextView ��ID  
        );
        register_listview.setAdapter(registerlistAdapter);
    }
	
	//����ģʽ�����RegistFragment ��ʵ��
	public static RegistFragment getInstance(){
		return registFragment;
	}
}
