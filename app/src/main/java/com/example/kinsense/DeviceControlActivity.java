package com.example.kinsense;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
    List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    DeviceAdapter deviceAdapter;
    private Handler handler;

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

        deviceAdapter = new DeviceAdapter(this, bluetoothDeviceList);
        listViewDevices.setAdapter(deviceAdapter);


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

    //scan results for SDK < 21
    private  BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //add device to list
                }
            });
        }
    };

    // scan results for SDK >= 21
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            //add Device to list
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };



    public void scanLeDevice(final boolean enable){

        final BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if(enable){

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    scanning = false;
                    if(Build.VERSION.SDK_INT < 21){
                        Log.d(TAG, "Scanning with BluetoohAdapter.LeScanCallBack < 21");
                        bluetoothAdapter.stopLeScan(leScanCallback);
                    }else{
                        Log.d(TAG, "Scanning with ScanCallBack >= 21 ");
                        bluetoothLeScanner.stopScan(scanCallback);
                    }
                }
            }, SCAN_PERIOD);

            scanning = true;
            if(Build.VERSION.SDK_INT < 21){
                Log.d(TAG, "Scanning with BluetoohAdapter.LeScanCallBack < 21");
                bluetoothAdapter.startLeScan(leScanCallback);
            }else{
                Log.d(TAG, "Scanning with ScanCallBack >= 21 ");
                bluetoothLeScanner.startScan(scanCallback);
            }

        }else{
            scanning = false;
            if(Build.VERSION.SDK_INT < 21){
                Log.d(TAG, "Scanning with BluetoohAdapter.LeScanCallBack < 21");
                bluetoothAdapter.stopLeScan(leScanCallback);
            }else{
                Log.d(TAG, "Scanning with ScanCallBack >= 21 ");
                bluetoothLeScanner.stopScan(scanCallback);
            }

        }
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
