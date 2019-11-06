package com.example.kinsense;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Scan extends AppCompatActivity {

    private ListView listView;
    private List<String> list = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;

    private Button b1;
    private TextView t1;

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        b1 = findViewById(R.id.button_scan);
        t1 = findViewById(R.id.textview_scanning_status);
        listView = findViewById(R.id.listview_devices);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //location access is required to be taken at run time for discovery
                ActivityCompat.requestPermissions(Scan.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                               MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

                Boolean bool = bluetoothAdapter.startDiscovery();
                Log.d("Discovery : ", String.valueOf(bool));
            }
        });

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(receiver, intentFilter);

        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, list);
        listView.setAdapter(arrayAdapter);

        listView.setClickable(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String s = listView.getItemAtPosition(position).toString();
                //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                t1.setText("Searching...");
                //Toast.makeText(getApplicationContext(), "Searching for devices..", Toast.LENGTH_SHORT).show();

            }else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                list.add(device.getName());
                Log.d("devices: ", device.getName());

                arrayAdapter.notifyDataSetChanged();
                //String deviceHardwareAddress = device.getAddress(); // get MAC address here
            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action) ){
                t1.setText("Finished");
                Toast.makeText(getApplicationContext(), "Discovery has finished", Toast.LENGTH_SHORT).show();

            }

        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
    }
}
