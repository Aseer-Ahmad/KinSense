package com.example.kinsense;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public class DeviceAdapter extends BaseAdapter {

    Context context;
    List<BluetoothDevice> listBluetoothDevices;
    LayoutInflater layoutInflater;

    public DeviceAdapter(Context context, List<BluetoothDevice> listBluetoothDevices) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.listBluetoothDevices = listBluetoothDevices;
    }

    @Override
    public int getCount() {
        return listBluetoothDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return listBluetoothDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewGroup view;
        if(convertView != null)
            view = (ViewGroup) convertView;
        else
            view = (ViewGroup) layoutInflater.inflate(R.layout.item_device_list, null);

        BluetoothDevice bluetoothDevice = listBluetoothDevices.get(position);

        return view;
    }
}
