package com.example.bluetoothdemo;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothService extends Service {
    private BluetoothDevice device;
    private BluetoothGatt bluetoothGatt;
    private BluetoothManager manager;
    private BluetoothAdapter adapter;

    private ControlActivity bindActivity;
    private final IBinder binder = new myBinder();

//    广播action
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static String ACTION_COMMUNICATION_SUCCESS=
            "com.example.bluetooth.le.ACTION_COMMUNICATION_SUCCESS";
    public final static String ACTION_GETPARA_SUCCESS=
            "com.example.bluetooth.le.ACTION_GETPATA_SUCCESS";
    public final static String CAMERA_0="加能";
    public final static String CAMERA_1="尼康";
    public final static String CAMERA_2="索尼";
    public final static String CAMERA_3="宾德";
    public final static String CAMERA_4="柯尼卡";
    public final static String CAMERA_5="美能达";
    public final static int START_COMMEND_RIGHT=0;
    public final static int START_COMMEND_LEFT=1;
    public final static int START_COMMEND_SETTING=2;

    public final static int ENGINE_PARA = 64;

    private int connectStatus= STATUS_DISCONNECTED;
    private boolean isConnected=false;
    private String dev_name;
    private String dev_address;
    private List<BluetoothGattCharacteristic> readableGc;
    private List<BluetoothGattCharacteristic> writeableGc;

    public static final int STATUS_CONNECTED = 0;
    public static final int STATUS_CONNECTTING = 1;
    public static final int STATUS_DISCONNECTED = 2;



    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int bt_status = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch(bt_status){
                        case BluetoothAdapter.STATE_TURNING_ON:
                            Log.d("Debug","turning on");
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            Log.d("Debug","turning off");
                            break;
                        case BluetoothAdapter.STATE_ON:
                            Log.d("Debug","on");
                            initialize();
                            connect(bindActivity.dev_address);
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            Log.d("Debug","off");
                            break;
                    }
                    break;

            }
        }
    };

    public boolean registerBroadcast() {
        IntentFilter filter = BluetoothService.getBroadcastFilter();
        if (receiver == null)
            return false;
        if (filter == null)
            return false;
        registerReceiver(receiver, filter);
        return true;
    }

    public void printToast(String data){
        final String s = data;
        bindActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(bindActivity, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

//    实现Gatt回调类
    private BluetoothGattCallback  gattCallback=new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if(newState == BluetoothProfile.STATE_CONNECTED){
                Log.d("Debug","连接到Gatt。");
                intentAction=ACTION_GATT_CONNECTED;
                connectStatus=STATUS_CONNECTED;
                broadcastUpdate(intentAction);
                bluetoothGatt.discoverServices();
            }else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                Log.d("Debug","断开Gatt连接。");
                intentAction=ACTION_GATT_DISCONNECTED;
                connectStatus=STATUS_DISCONNECTED;
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            List<BluetoothGattService> supportedServices=gatt.getServices();

            for(BluetoothGattService service:supportedServices){
                Log.e(service.getUuid().toString(),"!!!!!!!!下面是这条Service中的Characteristic!!!!!!!!");
                try{
                    Thread.currentThread().sleep(100);
                }catch(Exception e){
                    e.printStackTrace();
                }
                List<BluetoothGattCharacteristic> supportedGc=service.getCharacteristics();
                for(BluetoothGattCharacteristic characteristic: supportedGc){
                    if(characteristic.getProperties()==BluetoothGattCharacteristic.PROPERTY_READ){
                        Log.e(characteristic.getUuid().toString(),"这条characteristic可读。");
                        readableGc.add(characteristic);
                    }else if(characteristic.getProperties()==BluetoothGattCharacteristic.PROPERTY_WRITE){
                        Log.e(characteristic.getUuid().toString(),"这条characteristic可写。");
                        writeableGc.add(characteristic);
                    }else{
                        Log.e(characteristic.getUuid().toString(),"这条characteristic的属性值是："+characteristic.getProperties());
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            Log.v("Log","onCharacteristicRead()已经触发。");
            String strResult =characteristic.getStringValue(0);
            int intResult=characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,0);
            byte[] bytesResult =characteristic.getValue();
            Log.v("接受到的数据为： ","String "+strResult);
            Log.v("接受到的数据为： ","Integer "+intResult);
            Log.v("接受到的数据为： ","bytes "+bytesResult);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.v("Log","数据已经写入.");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            int dataLength=0;

            Log.v("Log","onCharacteristicChanged()已经触发。");
            String strResult =characteristic.getStringValue(0);
//            int intResult=characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,0);
            byte[] bytesResult =characteristic.getValue();
            dataLength=bytesResult.length;
            Log.v("接受到的数据为： ","String "+strResult);
//            Log.v("接受到的数据为： ","Integer "+intResult);
            String temp="";
            for(byte b:bytesResult){
                temp+=Integer.toHexString(b)+" ";
            }
            Log.v("接受到的数据为： ","bytes "+temp);
            Log.v("Debug","数据长度为："+dataLength+"byte");
            if(dataLength==1){
                if(bytesResult[0]==3){
                    Log.d("Debug","接收到确认码。");
                    broadcastUpdate(ACTION_COMMUNICATION_SUCCESS);
                }
            }
            if(dataLength == 12){
                byte sum = 0;
                int runStatus=0;
                int angle=0;
                int steps=0;
                int focus=0;
                int shot=0;
                int camera=0;
                Bundle bundle=new Bundle();
                for(byte b:bytesResult ){
                    sum+=b;
                }
//                if(sum == 0){
                    runStatus=bytesResult[1];
                    angle=((bytesResult[2]& 0xff)<<8 )^(bytesResult[3]&0xff);
                    steps=((bytesResult[4]& 0xff)<<8 )^(bytesResult[5]&0xff);
                    focus=((bytesResult[6]& 0xff)<<8 )^(bytesResult[7]&0xff);
                    shot=((bytesResult[8]& 0xff)<<8 )^(bytesResult[9]&0xff);
                    camera=bytesResult[10];
                    bundle.putInt("angle",angle);
                    bundle.putInt("runStatus",runStatus);
                    bundle.putInt("steps",steps);
                    bundle.putInt("focus",focus);
                    bundle.putInt("shot",shot);
                    bundle.putInt("camera",camera);
                    bundle.putString("DATA_TYPE","Response_parameters");

                    broadcastUpdate(ACTION_GETPARA_SUCCESS,bundle);
                    Log.d("Debug","广播已发送, 数据为：方式 "+runStatus+" 角度 "+angle+" 步数 "+steps+" 对焦 "+focus+" 拍摄 "+shot+" 相机 "+camera);
//                }
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

    };

//    .................................................................

    public void setBindActivity(ControlActivity activity){
        this.bindActivity = activity;
    }

    public class myBinder extends Binder{
        public myBinder(){

        }

        public BluetoothService getService(){
            return BluetoothService.this;
        }

    }


    @Override
    public void onCreate() {
        Log.v("LOG","Service is created.");
        super.onCreate();
        readableGc=new ArrayList<BluetoothGattCharacteristic>();
        writeableGc=new ArrayList<BluetoothGattCharacteristic>();

    }

    public boolean initialize(){

        manager=(BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        adapter=manager.getAdapter();
        if(manager == null){
            Log.v("Log","Manager获取失败。");
            return false;
        }
        if(adapter == null){
            Log.v("Log","适配器获取失败。");
            return false;
        }
        return true;
    }

    public boolean connect(String address){
        if(address == null && address.length()<=0){
            Log.e("Error: ","Invalid Address");
            return false;
    }

        device=adapter.getRemoteDevice(address);
        bluetoothGatt = device.connectGatt(this,false, gattCallback);
        if(check()){
            isConnected=true;
            Log.e("Debug","Gatt连接创建成功。");
            return true;
        }else{
            return false;
        }
    }

    public void disconnect(){
        if(manager == null){
            Log.v("Log","Manager获取失败。");
        }
        if(adapter == null){
            Log.v("Log","适配器获取失败。");
        }
        bluetoothGatt.disconnect();

    }



    public boolean writeCharacteristic(byte[] data){
//        int value=Integer.parseInt(data,16);
        byte[] writeBytes;
        writeBytes=data;
        BluetoothGattService service=bluetoothGatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
        if(service == null){
            Log.e("Error","无法取得service");
            return false;
        }
        BluetoothGattCharacteristic gc=service.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
        if(gc == null){
            Log.e("Error","无法取得characteristic");
            return false;
        }
        bluetoothGatt.setCharacteristicNotification(gc,true);
        gc.setValue(writeBytes);
//        gc.setValue(BluetoothGattCharacteristic.FORMAT_UINT32,value,0);
        bluetoothGatt.writeCharacteristic(gc);
        Log.d("Debug", "writeCharacteristic: "+writeBytes);
        return true;
    }

    public boolean writeStopCommend(){
        byte[] writeByte=new byte[3];
        writeByte[0]=0x3A;
        writeByte[1]=0x30;
        writeByte[2]=encode((byte)(writeByte[0]+writeByte[1]));
        if(writeCharacteristic(writeByte)){
            return true;
        }
        return false;
    }

    public boolean writeReadCommend(){
        byte[] writeByte=new byte[3];
        writeByte[0]=0x3A;
        writeByte[1]= 0x33;
        writeByte[2]=encode((byte)(writeByte[0]+writeByte[1]));
        if(writeCharacteristic(writeByte)){
            return true;
        }
        return false;
    }

    public boolean writeStartCommend(int commend){
        byte[] writeByte = new byte[4];
        byte sum = 0;
        writeByte[0]=0x3A;
        writeByte[1]=0x31;
        if(commend==START_COMMEND_SETTING||commend==START_COMMEND_RIGHT||commend==START_COMMEND_LEFT){
            if(commend==START_COMMEND_RIGHT){
                writeByte[2]=0;
            }
            if(commend==START_COMMEND_LEFT){
                writeByte[2]=1;
            }
            if(commend==START_COMMEND_SETTING){
                writeByte[2]=2;
            }
            for(int i=0;i<writeByte.length-1;i++){
                sum+=writeByte[i];
                writeByte[3]=encode(sum);
            }
            Log.d("Debug","前2位和检验位的和为： "+(sum+writeByte[3]));
            if(writeCharacteristic(writeByte)){
                return true;
            }
        }
        Log.e("ERROR","参数错误");
        return false;
    }

    public boolean writeCharacteristic(int angle,int stops,int focusDelay,int shotDelay,boolean continues,boolean returns,boolean direction,String cameraType){
        byte[] writeByte=new byte[13];
        byte sum=0;
        int step = angle * ENGINE_PARA;
        for(byte b: writeByte){
            b=0x00;
        }
        writeByte[0]=0x3A;
        writeByte[1]=0x32;
//        运行方式字
        if(continues)
            writeByte[2]=(byte)(writeByte[2]|1);
        if(returns)
            writeByte[2]=(byte)(writeByte[2]|2);
        if(direction)
            writeByte[2]=(byte)(writeByte[2]|16);
//        旋转步数
        writeByte[3]=(byte)(step>>8);
        writeByte[4]=(byte)step;
//        旋转段数
        writeByte[5]=(byte)(stops>>8);
        writeByte[6]=(byte)stops;
//        对焦延迟
        writeByte[7]=(byte)(focusDelay>>8);
        writeByte[8]=(byte)focusDelay;
//        拍摄延迟
        writeByte[9]=(byte)(shotDelay>>8);
        writeByte[10]=(byte)shotDelay;
//        相机类型
        if(cameraType.equals(CAMERA_0))
            writeByte[11]=0;
        if(cameraType.equals(CAMERA_1))
            writeByte[11]=1;
        if(cameraType.equals(CAMERA_2))
            writeByte[11]=2;
        if(cameraType.equals(CAMERA_3))
            writeByte[11]=3;
        if(cameraType.equals(CAMERA_4))
            writeByte[11]=4;
        if(cameraType.equals(CAMERA_5))
            writeByte[11]=5;
//        验证字节
        for(int i=0;i<writeByte.length-1;i++){
            sum+=writeByte[i];
            writeByte[12]=encode(sum);
        }
        Log.d("Debug","前12位和检验位的和为： "+(sum+writeByte[12]));
        if(writeCharacteristic(writeByte)){
            return true;
        }
        return false;
    }

    public byte encode(byte b){
        byte data=b;
        data=(byte)(b^0xff);
        data+=1;
        return data;
    }

    public byte[] HexToBytes(byte[] b){
        if ((b.length % 2) != 0) {
            throw new IllegalArgumentException("数据不完整");
        }
        byte[] b2=new byte[(b.length)/2];
        byte[] temp=new byte[2];

        for(int i=0;i<b.length;i+=2){
            temp[0]=b[i];
            temp[1]=b[i+1];
            String item1=new String(temp);
            b2[i/2] = (byte)Integer.parseInt(item1,16);
        }
        b=null;
        return b2;
    }

    public byte[] toBytes(String data){
        if(data == null || data.trim().equals("")){
            return new byte[0];
        }
        byte[] b=new byte[data.length()/2];
        for(int i=0;i<data.length()/2;i++){
            b[i]=(byte)Integer.parseInt(data.substring(i*2,(i+1)*2),16);
        }
        return b;
    }


    public boolean check(){
        if(manager == null){
            Log.e("Error: ","Manager获取失败。");
            return false;
        }
        if(adapter == null){
            Log.e("Error: ","适配器获取失败。");
            return false;
        }
        if(device == null){
            Log.e("Error: ","获取设备失败。");
            return false;
        }
        if(bluetoothGatt == null){
            Log.e("Error: ","获取Gatt连接失败。");
            return false;
        }
        return true;
    }


    public static IntentFilter getBroadcastFilter(){
        IntentFilter filter=new IntentFilter();
        filter.addAction(ACTION_DATA_AVAILABLE);
        filter.addAction(ACTION_GATT_CONNECTED);
        filter.addAction(ACTION_GATT_DISCONNECTED);
        filter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
        filter.addAction(ACTION_COMMUNICATION_SUCCESS);
        filter.addAction(ACTION_GETPARA_SUCCESS);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return filter;
    }

    public void broadcastUpdate(String action){
        Intent intent=new Intent(action);
        sendBroadcast(intent);
    }

    public void broadcastUpdate(String action,Bundle bundle){
        Intent intent=new Intent(action);
        intent.putExtras(bundle);
        sendBroadcast(intent);
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.v("Log","Service is binded.");
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("Log","Service is started.");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.v("Log","Service is destroyed.");
        super.onDestroy();
        disconnect();
        unregisterReceiver(receiver);
        bluetoothGatt=null;
        adapter=null;
        manager = null;
        device =null;
    }


}
