package com.example.bluetoothdemo;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 0;
    private static final int PREMISSION_WRITE_STORAGE = 1;
    private static final int PREMISSION_READ_STORAGE = 2;

    private boolean isScanning = false;
    private boolean findDevice = false;
    private static final long SCAN_TIME = 5000L;

    private TextView conntection_status;
    private Button openBt;
    private Button startSearch;
    private BluetoothAdapter adapter;
    private BluetoothManager manager;
    private boolean bt_states;
    private BluetoothDevice myDevice;
//    private Button btStates;

    //    保存扫描到的设备的listview和adapter
//    private ListView listView_devices;
//    private ListDeviceAdapter deviceAdapter;
    private ProgressBar bar;

    private BluetoothLeScanner scanner;


    //    使用adapter.startLeScan方法
//    private BluetoothAdapter.LeScanCallback mleScanCallback = new BluetoothAdapter.LeScanCallback() {
//        @Override
//        public void onLeScan(final BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    deviceAdapter.addDevice(bluetoothDevice);
//                    deviceAdapter.notifyDataSetChanged();
//                }
//            });
//        }
//    };


    //    使用BluetoothScanner.startScan方法
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            myDevice = result.getDevice();
            if (myDevice.getName() != null && myDevice.getName().equals("MLT-BT05") && !findDevice) {
                findDevice = true;
                findDevice(myDevice);
            }

//            找到设备后在listview中更新数据
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    deviceAdapter.addDevice(device);
//                    deviceAdapter.notifyDataSetChanged();
//                }
//            });
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }
    };

    final String TAG = "LOG";

    //    获取权限
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Debug","位置权限获取成功");
                    // TODO request success
                }
                break;
            case PREMISSION_WRITE_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO request success
                    Log.d("Debug","写权限获取成功");
                }
                break;
            case PREMISSION_READ_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO request success
                    Log.d("Debug","读权限获取成功");
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        获取位置权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }else{
                Log.d("Debug","位置权限已经获取");
            }
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE )!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PREMISSION_WRITE_STORAGE);
            }else{
                Log.d("Debug","写权限已经获取");
            }
            if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE )!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PREMISSION_READ_STORAGE);
            }else{
                Log.d("Debug","读权限已经获取");
            }
        }
//         初始化变量
        manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();
        initializeBluetooth();


        conntection_status = findViewById(R.id.TV_Status);
        bar = findViewById(R.id.progressbar);
        openBt = findViewById(R.id.button_openBt);
        openBt.setOnClickListener(this);
        startSearch = findViewById(R.id.button_searchDevice);
        startSearch.setOnClickListener(this);

//        初始化蓝牙开关提示窗口
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bluetooth Setting Warning.");
        builder.setMessage("The Bluetooth function is disabled, press confirm to enable.");
        builder.setPositiveButton("confirm", new DialogInterface.OnClickListener() {
            //            设置确认按键效果
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (openBluetooth()) {
                    conntection_status.setText("Searching for Device");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scanDevice();
                        }
                    },500L);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            //            设置取消按键效果
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                dialogInterface.dismiss();
            }
        });

//        查看本机蓝牙状态的按钮
//        btStates = findViewById(R.id.button_states);
//        btStates.setOnClickListener(this);

//        初始化listView和listViewAdapter
//        deviceAdapter = new ListDeviceAdapter(this);
//        listView_devices = findViewById(R.id.Scan_Devices);
//        listView_devices.setAdapter(deviceAdapter);
//        listView_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                BluetoothDevice bluetoothDevice = (BluetoothDevice) deviceAdapter.getItem(i);
//                if (bluetoothDevice == null) return;
//                Intent intent = new Intent(MainActivity.this, ControlActivity.class);
//                intent.putExtra(ControlActivity.TAG_NAME, bluetoothDevice.getName());
//                intent.putExtra(ControlActivity.TAG_ADDRESS, bluetoothDevice.getAddress());
//                if (isScanning) {
//                    isScanning = false;
//                    scanner.stopScan(mScanCallback);
//                }
//                startActivity(intent);
//            }
//        });


//        检查手机蓝牙功能是否开启
        if (!adapter.isEnabled()) {
            conntection_status.setText("Bluetooth disabled.");
            builder.show();
        } else {
//        直接开始扫描目标设备
            conntection_status.setText("Searching for Device");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanDevice();
                }
            },500L);
        }


//        adjustTextForBt();


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //    初始化蓝牙相关变量
    public void initializeBluetooth() {

        scanner = adapter.getBluetoothLeScanner();
        bt_states = adapter.isEnabled();
    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public boolean openBluetooth() {

        if (adapter == null) {
            showToast("本设备不支持蓝牙。");
            return false;
        }

        if (adapter.isEnabled()) {
//            if (adapter.disable()) {
//                openBt.setText("打开蓝牙");
//                return true;
//            } else {
//                Toast.makeText(this, "操作失败", Toast.LENGTH_LONG);
//                return false;
//            }
            return true;
        } else {

            if (adapter.enable()) {
                return true;
            }
            showToast("Failed to enable Bluetooth.");
            return false;

        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_openBt:
//                showToast(adapter.isEnabled() + " " + (myDevice == null) + findDevice);
                if (adapter.isEnabled())
                    showToast("Bluetooth is already enabled.");
                else
                    if(openBluetooth())
                        conntection_status.setText("Bluetooth is enabled.");
                break;
            case R.id.button_searchDevice:
                if (!adapter.isEnabled()) {
                    showToast("Bluetooth is disabled, please enable.");
                    return;
                }
                if (!findDevice || myDevice == null) {
                    conntection_status.setText("Searching for Device");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scanDevice();
                        }
                    },500L);
                } else {
                    findDevice(myDevice);
                }
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x01) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "操作失败", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_OK) {
                showToast("Bluetooth enabled successfully.");
//                openBt.setText("关闭蓝牙");
            }
        }
    }

//    public void startScan(View view) {
//        setVisible();
//        if (adapter.isEnabled() == false) {
//            showToast("Please enable Bluetooth.");
//            return;
//        }
//        if (!isScanning) {
//            deviceAdapter.clearDeviceList();
//            deviceAdapter.notifyDataSetChanged();
//            scanDevice(true);
//        } else
//            return;
//    }

    public void scanDevice() {
        initializeBluetooth();
        if (scanner != null) {
            setVisible();
//            showToast("stop scanning after 5s");
            Handler myHandler = new Handler();
            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setInvisible();
                    isScanning = false;
                    scanner.stopScan(mScanCallback);
                    if (!findDevice || myDevice == null)
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.this.conntection_status.setText("Cannot find the device");
                                showToast("Cannot find the device, Please checkout the device.");
                            }
                        });
                }
            }, SCAN_TIME);
//            showToast("start scan");
            isScanning = true;
            scanner.startScan(mScanCallback);
            Log.v("LOG", "Start scanning");
//            else {
//                isScanning = false;
//                setInvisible();
//                scanner.stopScan(mScanCallback);
//                Log.v("LOG", "Stop scanning");
//            }
        } else {
            Log.v("LOG", "Failed to get scanner.");
            return;
        }
    }

    public void findDevice(BluetoothDevice device) {
        showToast("Device Found ! Switch to the Control Interface after 3s.");
        conntection_status.setText("Device Found.");
        final Intent intent = new Intent(this, ControlActivity.class);
        intent.putExtra(ControlActivity.TAG_NAME, device.getName());
        intent.putExtra(ControlActivity.TAG_ADDRESS, device.getAddress());
        Handler myHandler = new Handler();
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
            }
        }, 3000L);
    }


    public void adjustTextForBt() {
        if (bt_states) {
            openBt.setText("关闭蓝牙");
        } else {
            openBt.setText("打开蓝牙");
        }
    }


    public void setVisible() {
        bar.setVisibility(View.VISIBLE);
    }

    public void setInvisible() {
        bar.setVisibility(View.INVISIBLE);
    }
}
