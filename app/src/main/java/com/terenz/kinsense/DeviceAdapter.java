package com.terenz.kinsense;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class DeviceAdapter extends BaseAdapter {

    private Context context;
    private List<BluetoothDevice> listBluetoothDevices;
    private Map<String, Integer> devRssiValues ;
    private LayoutInflater layoutInflater;

    public DeviceAdapter(Context context, List<BluetoothDevice> listBluetoothDevices, Map<String, Integer> devRssiValues) {
        this.context = context;
        this.devRssiValues = devRssiValues;
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

        //get device
        BluetoothDevice bluetoothDevice = listBluetoothDevices.get(position);

        //get component Ids from item device list
        TextView paired = view.findViewById(R.id.textview_lepaired);
        TextView name = view.findViewById(R.id.textview_lename);
        TextView address = view.findViewById(R.id.textview_leaddress);
        TextView rssi = view.findViewById(R.id.textview_rssi);

        name.setText( bluetoothDevice.getName() );
        address.setText( bluetoothDevice.getAddress());

        byte rssiVal = (byte) devRssiValues.get(bluetoothDevice.getAddress()).intValue();
        if(rssiVal!=0)
            rssi.setText("Rssi = "+ String.valueOf(rssiVal));

        if(bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED){
            paired.setText("Paired");
            paired.setTextColor(Color.GREEN);
        }else{
            paired.setText("Not Paired");
            paired.setTextColor(Color.BLACK);
        }

        return view;
    }
}
