package com.terenz.kinsense;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceControlActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private final String TAG = DeviceControlActivity.class.getSimpleName();

    //resources
    Map<String, Integer> devRssiValues = new HashMap<String, Integer>();
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    DeviceAdapter deviceAdapter;
    private Handler handler  = new Handler();

    //constants & flags
    private static final long SCAN_PERIOD = 10000; //scanning for 10 seconds
    private boolean scanning = false;

    //id components
    ListView listViewDevices;
    Button buttonScan;
    TextView textViewScanningStatus;

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

        findComponents();

        deviceAdapter = new DeviceAdapter(this, bluetoothDeviceList, devRssiValues);
        listViewDevices.setAdapter(deviceAdapter);
        //set item click listener
        listViewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get device and send back to MainActivity using bundle and access in onActivityResult
                BluetoothDevice device = bluetoothDeviceList.get(position);
                scanLeDevice(false);


                Bundle b = new Bundle();
                b.putString(BluetoothDevice.EXTRA_DEVICE, device.getAddress() );
                Intent intent = new Intent();
                intent.putExtras(b);
                setResult(Activity.RESULT_OK, intent);
                finish();


            }
        });

        //request real time permission to access Location
        ActivityCompat.requestPermissions(DeviceControlActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 if(scanning == false)
                    scanLeDevice(true);
            }
        });

    }

    //scan results for SDK < 21
    private  BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //add device to list
                    addDevice(device, rssi);
                }
            });
        }
    };

    // scan results for SDK >= 21
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            //add device to list
            addDevice(result.getDevice(), result.getRssi());

        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "Scanning has Failed.");
        }
    };

    private void addDevice(BluetoothDevice device, int rssi){
        boolean deviceInList = false;

        for(BluetoothDevice device1 : bluetoothDeviceList){
            if(device1.getAddress().equals(device.getAddress())){
                deviceInList = true;
                break;
            }
        }

        devRssiValues.put(device.getAddress(), rssi);

        if(!deviceInList){
            bluetoothDeviceList.add(device);
            deviceAdapter.notifyDataSetChanged();
        }
    }


    public void scanLeDevice(final boolean enable){

        final BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if(enable){

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    scanning = false;
                    textViewScanningStatus.setText("Finished");
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
            textViewScanningStatus.setText("Scanning...");
            if(Build.VERSION.SDK_INT < 21){
                Log.d(TAG, "Scanning with BluetoohAdapter.LeScanCallBack < 21");
                bluetoothAdapter.startLeScan(leScanCallback);
            }else{
                Log.d(TAG, "Scanning with ScanCallBack >= 21 ");
                bluetoothLeScanner.startScan(scanCallback);
            }

        }else{
            scanning = false;
            textViewScanningStatus.setText("Finished");
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
        textViewScanningStatus =findViewById(R.id.textview_scanning_status);
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

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //scanLeDevice(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        scanLeDevice(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanLeDevice(false);
    }
}
