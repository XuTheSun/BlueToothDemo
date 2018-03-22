package com.example.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 关旭 on 2018/1/23.
 */

public class ListDeviceAdapter extends BaseAdapter {
    private List<BluetoothDevice> devices;
    private Context context;


    public ListDeviceAdapter(Context context){
        devices=new ArrayList<BluetoothDevice>();
        this.context=context;
    }

    public void clearDeviceList(){
        devices.clear();
    }

    public List<BluetoothDevice> getDeviceList(){
        return devices;
    }

    public void addDevice(BluetoothDevice device){
        if(devices.contains(device)){
            Log.v("Device Repeated","Repeated address "+device.getAddress());
            return;
        }
        devices.add(device);
        Log.v("Device added",""+device.getAddress());
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextHolder holder=new TextHolder();
        Log.v("Log","running getView()");
        if(view == null){
            view= LayoutInflater.from(context).inflate(R.layout.device_detail_layout,null,false);
            Log.v("LOG",view==null?"view is null":"view is not null");
            holder.name=(TextView) view.findViewById(R.id.textView_name);
            holder.address=(TextView) view.findViewById(R.id.textView_address);
            view.setTag(holder);
            Log.v("LOG","view "+i+" is Null");
        }else{
            holder=(TextHolder)view.getTag();
            Log.v("LOG","view "+i+" is not Null");
        }
        BluetoothDevice device=devices.get(i);
        String deviceName=device.getName();
        if(deviceName != null ){
            holder.name.setText(deviceName);
        }else
            holder.name.setText("Unknown Device");
        holder.address.setText(device.getAddress());

        return view;
    }



    @Override
    public Object getItem(int i) {
        return devices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


    @Override
    public int getCount() {
        return devices.size();
    }



}
