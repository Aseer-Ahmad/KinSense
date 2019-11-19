package com.example.kinsense;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceControlActivity extends AppCompatActivity {

    private final String TAG = DeviceControlActivity.class.getSimpleName();

    //resources
    Map<String, Integer> devRssiValues = new HashMap<String, Integer>();
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    List<BluetoothDevice> bluetoothDeviceList;
    //ArrayAdapter<BluetoothDevice> arrayAdapter;


    //constants & flags
    private static final long SCAN_PERIOD = 10000; //scanning for 10 seconds
    private boolean scanning ;

    //id components
    ListView listViewDevices;
    Button buttonScan;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        getBluetoothAdapter();

        bluetoothDeviceList = new ArrayList<BluetoothDevice>();
        arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, bluetoothDeviceList);
        listViewDevices.setAdapter(arrayAdapter);


        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(scanning == false){
                    //start scan here

                }else{

                }
            }
        });





    }

    public void findComponents(){
        listViewDevices = findViewById(R.id.listview_discoverd_devices);
        buttonScan = findViewById(R.id.button_scan);

    }

    public void getBluetoothAdapter(){
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }
}