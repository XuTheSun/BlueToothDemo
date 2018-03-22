package com.example.bluetoothdemo;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.File;
import java.util.Arrays;


public class ControlActivity extends AppCompatActivity {
    public static final String TAG_NAME = "name";
    public static final String TAG_ADDRESS = "address";
    public static final int WRITE_WITH_SETTINGS = 0;
    public static final int WRITE_FREELY = 1;
    public static final int DATA_WRITE_TURN = 0;
    public static final int DATA_WRITE_STOP = 1;
    public static final int DATA_WRITE_SETTING = 2;
    public static final int DATA_WRITE_COMPLETE = 3;
    public static final int DATA_READ = 4;

    public String dev_address;
    public String dev_name;

    private int rotate_angle = 0;
    private int rotate_stops = 0;
    private int focus_delay = 0;
    private int shot_delay = 0;
    private String camera_type;
    private boolean continues;
    private boolean returns;
    private boolean direction;
    private int camera_code;

    private int actionStatus;

    private TextView view_name;
    private TextView view_address;
    private TextView view_connectStatus;

//    private Toolbar toolbar;
//    private EditText view_data;
//    private Button btn_write;

    private Button btn_settings;
    private Button btn_left;
    private Button btn_right;
    private Button btn_read;
    private Button btn_preload;
    private Button btn_editPreload;
    private EditText editText_angle;
    private EditText editText_stops;
    private EditText editText_focusDelay;
    private EditText editText_shotDelay;
    private CheckBox box_continues;
    private CheckBox box_returns;
    private CheckBox box_direction;
    private Spinner spinner_cameraType;

    private SQLiteDatabase database;
    private Cursor data;
    private SQLiteUtils sqLiteUtils;
    private MyOpenHelper myHelper;
    //    用于储存预设值的名称
    private String[] names;

    private BluetoothService service;

    public void initialDataBase() {

        sqLiteUtils = SQLiteUtils.getInstance();
        myHelper = new MyOpenHelper(this, "myDb.db3", null, 1);
        database = myHelper.getReadableDatabase();
        Log.d("Debug", "数据库初始化成功。");
        initialData();
        Log.d("Debug", "Curse初始化成功。");
    }

    public void closeDatabase() {
        data.close();
        database.close();
        myHelper.close();
    }

    public void initialNames() {
        if (data.moveToFirst()) {
            names = new String[data.getCount()];
            for (int i = 0; i < data.getCount(); i++) {
                Log.d("DebugCount...", "" + data.getCount());
                Log.d("Debug", "+" + i);
                data.moveToPosition(i);
                names[i] = data.getString(0);

            }
        } else {
            names = new String[0];
            Log.e("Error", "初始化名称字符串出错");
        }
    }

    public void initialData() {
        data = sqLiteUtils.getData(database);
        String[] names = data.getColumnNames();
        if (names.length > 0 && names != null) {
            for (String name : names) {
                Log.d("Debug Names---------", name);
            }
        }
        initialNames();
    }

    public String[] addAtTheEnd(String[] name) {
        String result[];
        if (name != null && name.length != 0) {
            result = Arrays.copyOf(name, name.length + 1);
        } else {
            result = new String[1];
        }
        result[result.length - 1] = "添加新的值";
        return result;
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {

                case R.id.button_setting:
                    actionStatus = DATA_WRITE_SETTING;
                    if (writeData(WRITE_WITH_SETTINGS)) {
                        Log.d("Debug", "数据格式无误，写入完成。");
                    }
                    break;

                case R.id.button_read:
                    if (service.writeReadCommend()) {
                        Log.d("Debug", "参数读取指令写入成功.");
                        actionStatus = DATA_READ;
                    }
                    break;

                case R.id.button_edit_preload:

                    initialData();
                    if (initializeParameters()) {

                        final String[] saveNames = addAtTheEnd(names);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ControlActivity.this);
                        builder.setTitle("保存为").setAdapter(new ArrayAdapter<String>(ControlActivity.this,
                                        R.layout.preload_layout, saveNames),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (i == (saveNames.length - 1)) {
                                            LayoutInflater inflater = LayoutInflater.from(ControlActivity.this);
                                            final View dialogView = inflater.inflate(R.layout.new_preload, null);
                                            AlertDialog.Builder builder1 = new AlertDialog.Builder(ControlActivity.this);
                                            builder1.setTitle("请命名").setView(dialogView).
                                                    setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {

                                                            EditText preloadName = dialogView.findViewById(R.id.editText_newName);

                                                            String name = preloadName.getText().toString();
                                                            Log.d("Debug", "name" + name);
                                                            if (name.equals("") || name == null) {
                                                                Toast.makeText(ControlActivity.this, "请输入新的预设值名称", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                try {
                                                                    sqLiteUtils.insert(database, name, rotate_angle, rotate_stops, focus_delay, shot_delay,
                                                                            camera_type, continues, returns, direction);
                                                                    Toast.makeText(ControlActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                                                                } catch (SQLiteException se) {
                                                                    se.printStackTrace();
                                                                    Log.e("Error", se.getMessage());
                                                                    Toast.makeText(ControlActivity.this, "该名称的项已经存在。", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }

                                                        }
                                                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            });
                                            builder1.show();
                                        } else {
                                            checkDupAndUpdate(i);
                                        }
                                    }
                                });
                        builder.show();
                    }
                    break;

                case R.id.button_preload:
                    initialData();

                    if (!data.moveToFirst() || names == null || names.length <= 0) {
                        Toast.makeText(ControlActivity.this, "您没有设置任何预设值", Toast.LENGTH_SHORT).show();
                    } else {

                        View preloadView = LayoutInflater.from(ControlActivity.this).inflate(R.layout.preload_list_layout, null);
                        ListView preloadList = preloadView.findViewById(R.id.preload_list);
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(ControlActivity.this);
                        builder2.setTitle("请选择预设值").setView(preloadView);
                        AlertDialog dialog = builder2.create();
                        PreloadListAdapter adapter = new PreloadListAdapter(ControlActivity.this, names, dialog);
                        preloadList.setAdapter(adapter);
                        dialog.show();

                    }
                    break;
            }
        }
    };

    public boolean deleteItem(int i) {
        data.moveToPosition(i);
        String name = names[i];
        try {
            sqLiteUtils.delete(database, name);
            return true;
        } catch (SQLException se) {
            Log.e("Error", se.getMessage());
            return false;
        }
    }

    public boolean updateUi(int i) {
        data.moveToPosition(i);
        rotate_angle = data.getInt(1);
        rotate_stops = data.getInt(2);
        focus_delay = data.getInt(3);
        shot_delay = data.getInt(4);
        camera_type = data.getString(5);
        if (camera_type.equals("加能")) camera_code = 0;
        else if (camera_type.equals("尼康")) camera_code = 1;
        else if (camera_type.equals("索尼")) camera_code = 2;
        else if (camera_type.equals("宾德")) camera_code = 3;
        else if (camera_type.equals("柯尼卡")) camera_code = 4;
        else if (camera_type.equals("美能达")) camera_code = 5;
        int contInt, rtnInt, dirInt;
        contInt = data.getInt(6);
        rtnInt = data.getInt(7);
        dirInt = data.getInt(8);
        continues = (contInt == 1) ? true : false;
        returns = (rtnInt == 1) ? true : false;
        direction = (dirInt == 1) ? true : false;
        if (updatePara()) return true;
        return false;
    }

    public void checkDupAndUpdate(int index) {

        initializeParameters();
        data.moveToPosition(index);
        String name = names[index];

        int contInt, rtnInt, dirInt;
        contInt = continues ? 1 : 0;
        rtnInt = returns ? 1 : 0;
        dirInt = direction ? 1 : 0;
        Log.d("Debug", rotate_angle + " " + rotate_stops + " " + focus_delay + " " + shot_delay + " " + camera_type + " " + continues + " " + returns + " " + direction + " " + contInt + " " + rtnInt + " " + dirInt);

        Log.d("Debug", data.getInt(1) + " " + data.getInt(2) + " " + data.getInt(3) + " " + data.getInt(4) + " " + data.getString(5) + " " + data.getInt(6) + " " + data.getInt(7) + " " + data.getInt(8));

//        if (data.moveToFirst()) {
//            for (int i = 0; i < data.getCount(); i++) {
        if (rotate_angle != data.getInt(1) ||
                rotate_stops != data.getInt(2) ||
                focus_delay != data.getInt(3) ||
                shot_delay != data.getInt(4) ||
                !camera_type.equals(data.getString(5)) ||
                contInt != data.getInt(6) ||
                rtnInt != data.getInt(7) ||
                dirInt != data.getInt(8)) {
            try {
                sqLiteUtils.update(database, name, rotate_angle, rotate_stops, focus_delay, shot_delay,
                        camera_type, continues, returns, direction);
                Toast.makeText(ControlActivity.this, "成功更新 " + name, Toast.LENGTH_SHORT).show();
            } catch (SQLiteException se) {
                se.printStackTrace();
            }
        } else {
            Log.d("Debug", "当前数据和数据库中的数据没有区别，无需更新。");
            Toast.makeText(ControlActivity.this, "当前数据没有区别，无需更新。", Toast.LENGTH_SHORT).show();
        }
//            }
//        }
    }

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (view.getId()) {
                case R.id.button_left:
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        if (service.writeStartCommend(BluetoothService.START_COMMEND_LEFT)) {
                            Log.d("Debug", "指令写入成功，左转");
                            actionStatus = DATA_WRITE_TURN;
                            return true;
                        }

                    }
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        if (service.writeStopCommend()) {
                            Log.d("Debug", "指令写入成功，停止。");
                            actionStatus = DATA_WRITE_STOP;
                            return true;
                        }
                    }
                    break;
                case R.id.button_right:
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        if (service.writeStartCommend(BluetoothService.START_COMMEND_RIGHT)) {
                            Log.d("Debug", "指令写入成功，右转");
                            actionStatus = DATA_WRITE_TURN;
                            return true;
                        }
                    }
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        if (service.writeStopCommend()) {
                            Log.d("Debug", "指令写入成功，停止。");
                            actionStatus = DATA_WRITE_STOP;
                            return true;
                        }
                    }
                    break;
            }
            return false;
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e("awdawdawd", "GUANXU");
            service = ((BluetoothService.myBinder) iBinder).getService();
            service.setBindActivity(ControlActivity.this);
            service.registerBroadcast();
            Log.v("Log", "Service is connected with the activity.");
            if (!service.initialize()) {
                Log.e("Error", "初始化失败，请检查设备。");
                finish();
            }
            if (!service.connect(dev_address.trim())) {
                Log.e("Error", "连接失败");
                finish();
            } else {
                Log.d("Debug", "连接已经打开。");
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.v("Log", "Service is disconnected.");
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Debug", "actionStatus: " + actionStatus);
            if (!intent.getAction().equals("") && intent.getAction().equals(BluetoothService.ACTION_GATT_CONNECTED)) {
                setConnectionStatus(true);
            } else if (!intent.getAction().equals("") && intent.getAction().equals(BluetoothService.ACTION_GATT_DISCONNECTED)) {
                setConnectionStatus(false);
            } else if (!intent.getAction().equals("") && intent.getAction().equals(BluetoothService.ACTION_COMMUNICATION_SUCCESS)) {
                if (actionStatus == DATA_WRITE_SETTING) {
                    if (service.writeStartCommend(BluetoothService.START_COMMEND_SETTING)) {
                        actionStatus = DATA_WRITE_COMPLETE;
                        Log.d("Debug", "指令写入成功，按照当前参数运行程序。");
                    }
                }
                if (actionStatus == DATA_READ) {
                    Log.d("Debug", "得到确认数据，读取指令传输成功。");
                }
            } else if (!intent.getAction().equals("") && intent.getAction().equals(BluetoothService.ACTION_GETPARA_SUCCESS)) {
                Log.d("Debug", "识别到GETPARA广播.");
                Bundle bundle = intent.getExtras();
                int runStatus = bundle.getInt("runStatus");
                rotate_angle = bundle.getInt("angle") / 64;
                rotate_stops = bundle.getInt("steps");
                focus_delay = bundle.getInt("focus");
                shot_delay = bundle.getInt("shot");
                camera_code = bundle.getInt("camera");
                if ((runStatus & 0x01) == 0)
                    continues = false;
                else if ((runStatus & 0x01) == 1)
                    continues = true;

                if ((runStatus >> 1 & 0x01) == 0)
                    returns = false;
                else if ((runStatus >> 1 & 0x01) == 1)
                    returns = true;

                if ((runStatus >> 4 & 0x01) == 0)
                    direction = false;
                else if ((runStatus >> 4 & 0x01) == 1)
                    direction = true;
                updatePara();
            }

        }
    };


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_control, menu);
//        return true;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
//        toolbar = findViewById(R.id.bar);
//        toolbar.inflateMenu(R.menu.menu_control);
//        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.dev_connect:
//                        service.initialize();
//                        service.connect(dev_address);
//                        break;
//                    case R.id.dev_disconnect:
//
//                        break;
//                }
//                return true;
//            }
//        });
        view_name = findViewById(R.id.control_name);
        view_address = findViewById(R.id.control_address);
        view_connectStatus = findViewById(R.id.control_status);

        btn_settings = findViewById(R.id.button_setting);
        btn_settings.setOnClickListener(listener);
        btn_left = findViewById(R.id.button_left);
        btn_right = findViewById(R.id.button_right);
        btn_read = findViewById(R.id.button_read);
        btn_read.setOnClickListener(listener);
        btn_left.setOnTouchListener(touchListener);
        btn_right.setOnTouchListener(touchListener);
        btn_preload = findViewById(R.id.button_preload);
        btn_editPreload = findViewById(R.id.button_edit_preload);
        btn_editPreload.setOnClickListener(listener);
        btn_preload.setOnClickListener(listener);

        editText_angle = findViewById(R.id.rotate_angle);
        editText_focusDelay = findViewById(R.id.focus_delay);
        editText_shotDelay = findViewById(R.id.shot_delay);
        editText_stops = findViewById(R.id.rotate_stops);

        box_continues = findViewById(R.id.checkBox_continus);
        box_direction = findViewById(R.id.checkBox_direction);
//        box_direction.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(direction){
//                    box_direction.setText("右转");
//                }else{
//                    box_direction.setText("左转");
//                }
//            }
//        });
        box_returns = findViewById(R.id.checkBox_returns);
        spinner_cameraType = findViewById(R.id.preload_camera_type);

//        preloadSettingList = new ArrayList<>();

        setConnectionStatus(false);

        Log.v(view_name == null ? "null" : "not Null", view_address == null ? "null" : "Not null");
        Intent intent = getIntent();
        dev_name = intent.getStringExtra(TAG_NAME);
        dev_address = intent.getStringExtra(TAG_ADDRESS);
        Log.v(dev_name, dev_address);
        if (view_name != null && view_address != null) {
            if (dev_name != null && dev_name.length() > 0) {
                view_name.setText(view_name.getText().toString() + dev_name.trim());
            }
            if (dev_address != null && dev_name.length() > 0)
                view_address.setText(view_address.getText().toString() + dev_address.trim());
        }
        bindBluetoothService();

        Log.d("Debug", getApplicationContext().getFilesDir().getAbsolutePath().replace("files", "databases") + File.separator);

//        testFile();

        initialDataBase();

//        inititalPreloads();


    }


    public void bindBluetoothService() {
        Intent intent_Service = new Intent(this, BluetoothService.class);
        if (bindService(intent_Service, serviceConnection, BIND_AUTO_CREATE))
            Log.d("Debug", "绑定服务。");
        else
            Log.d("Debug", "绑定服务出错");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (registerBroadcast())
            Log.d("Debug", "广播注册成功。");
        else
            Log.e("Error", "广播注册失败。");
        if (service != null) {
            service.connect(dev_address);
        }
        initialDataBase();
    }


    public boolean registerBroadcast() {
        IntentFilter filter = BluetoothService.getBroadcastFilter();
        if (receiver == null)
            return false;
        if (filter == null)
            return false;
        registerReceiver(receiver, filter);
        return true;
    }

    public void setConnectionStatus(boolean connect) {

//        Menu menu = toolbar.getMenu();
//        if (menu == null) {
//            getMenuInflater().inflate(R.menu.menu_control, menu);
//        }
//
//        MenuItem connect_btn = menu.findItem(R.id.dev_connect);
//        MenuItem disconnect_btn = menu.findItem(R.id.dev_disconnect);

        if (connect) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view_connectStatus.setText("连接状态： connected");
                }
            });
//            connect_btn.setVisible(false);
//            disconnect_btn.setVisible(true);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view_connectStatus.setText("连接状态： disconnected");
                }
            });
//            connect_btn.setVisible(true);
//            connect_btn.setVisible(false);
        }

    }

    public boolean writeData(int i) {
        switch (i) {
//            case WRITE_FREELY:
//                String data = view_data.getText().toString();
//                if (!service.writeCharacteristic(service.HexToBytes(data.getBytes()))) {
//                    Log.e("Error", "BluetoothService Write方法出现问题。");
//                    return false;
//                }
//                Log.d("Debug", "写入的数据为: " + data);
//                return true;
            case WRITE_WITH_SETTINGS:
                if (!initializeParameters()) {
                    Log.e("Error", "参数填写有误，请检查。");
                    return false;
                }

                if (!service.writeCharacteristic(rotate_angle, rotate_stops, focus_delay, shot_delay, continues, returns, direction, camera_type)) {
                    Log.e("Error", "BluetoothService Write方法出现问题。");
                    return false;
                }
                Log.d("Debug", "数据写入成功。");

                return true;

        }
        return false;
    }

    public boolean initializeParameters() {
        if (editText_angle.getText().toString().equals("") ||
                editText_stops.getText().toString().equals("") ||
                editText_shotDelay.getText().toString().equals("") ||
                editText_focusDelay.getText().toString().equals("")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ControlActivity.this, "请设置全部参数。", Toast.LENGTH_LONG).show();
                }
            });
            return false;
        }
        try {
            rotate_angle = Integer.parseInt(editText_angle.getText().toString());
            rotate_stops = Integer.parseInt(editText_stops.getText().toString());
            focus_delay = Integer.parseInt(editText_focusDelay.getText().toString());
            shot_delay = Integer.parseInt(editText_shotDelay.getText().toString());
        } catch (NumberFormatException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ControlActivity.this, "请在前四个参数位置填写数字。", Toast.LENGTH_LONG).show();
                }
            });
            return false;
        }
        camera_type = spinner_cameraType.getSelectedItem().toString();

        continues = box_continues.isChecked() ? true : false;

        returns = box_returns.isChecked() ? true : false;
//        旋转方向，选中为左，不选为右
        direction = box_direction.isChecked() ? true : false;
//        参数检测
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(ControlActivity.this,"角度："+rotate_angle+",段数： "+rotate_stops+",对焦延时： "+focus_delay+",拍摄延时： " +
//                        shot_delay+"，相机： "+camera_type+", 连续： "+continues+", 往返： "+returns+",方向： "+direction,Toast.LENGTH_LONG).show();
//            }
//        });
        return true;

    }

    public boolean updatePara() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editText_angle.setText(rotate_angle + "");
                editText_stops.setText(rotate_stops + "");
                editText_focusDelay.setText(focus_delay + "");
                editText_shotDelay.setText(shot_delay + "");
                spinner_cameraType.setSelection(camera_code);

                if (continues)
                    box_continues.setChecked(true);
                else
                    box_continues.setChecked(false);

                if (returns)
                    box_returns.setChecked(true);
                else
                    box_returns.setChecked(false);

                if (direction)
                    box_direction.setChecked(true);
                else
                    box_direction.setChecked(false);
            }
        });
        return true;
    }


//    public boolean writeData(){
//
//        return true;
//    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        closeDatabase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        closeDatabase();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

//    private static final String TAG_EXIT = "exit";
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        if (intent != null) {
//            boolean isExit = intent.getBooleanExtra(TAG_EXIT, false);
//            if (isExit) {
//                this.finish();
//            }
//        }
//    }

    private Long currentTime = 0L;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - currentTime < 2000L) {
                service.disconnect();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else {
                currentTime = System.currentTimeMillis();
                Toast.makeText(this, "Press again to exit.", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onKeyDown(keyCode, event);
    }


}
