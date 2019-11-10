package com.example.kinsense;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Scan extends AppCompatActivity {

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ListView listViewDiscovered;
    private ListView listViewPaired;
    private List<String> listDiscovered = new ArrayList<>();
    private List<String> listPaired = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapterDiscovered;
    private ArrayAdapter<String> arrayAdapterPaired;
    private Map<String, BluetoothDevice> mapDiscovered = new HashMap<String, BluetoothDevice>();
    private Map<String, BluetoothDevice> mapPaired = new HashMap<String, BluetoothDevice>();

    private Button b1;
    private TextView t1;

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

    static final int STATE_CONNECTED = 1;
    static final int STATE_CONNECTION_FAILED = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        b1 = findViewById(R.id.button_scan);
        t1 = findViewById(R.id.textview_scanning_status);
        listViewDiscovered = findViewById(R.id.listview_discoverd_devices);
        listViewPaired = findViewById(R.id.listview_paired_devices);

        b1.setOnClickListener(new View.OnClickListener() {  // find your device
            @Override
            public void onClick(View v) {

                listDiscovered.clear();
                showPairedDevices();

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

        showDiscoveredDevices(); // show discoverd in listViewDiscovered



    }

    private void showDiscoveredDevices() {
        arrayAdapterDiscovered = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, listDiscovered);
        listViewDiscovered.setAdapter(arrayAdapterDiscovered);

        listViewDiscovered.setClickable(true);
        listViewDiscovered.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String s = listViewDiscovered.getItemAtPosition(position).toString();
                BluetoothDevice bluetoothDevice = mapDiscovered.get(s);
                Toast.makeText(getApplicationContext(),s+" : "+bluetoothDevice.getAddress(), Toast.LENGTH_SHORT).show();

                //connect to the device here

                ClientThread clientThread = new ClientThread(bluetoothDevice);
                clientThread.start();

            }
        });
    }

    private void showPairedDevices() {
        listPaired.clear();
        arrayAdapterPaired = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_expandable_list_item_1,
                                                                        listPaired );
        listViewPaired.setAdapter(arrayAdapterPaired);
        listViewPaired.setClickable(true);
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        int totaldevices = pairedDevices.size();

        if (totaldevices > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                //String deviceName = device.getName();
                //String deviceHardwareAddress = device.getAddress(); // MAC address

                listPaired.add(device.getName());
                arrayAdapterPaired.notifyDataSetChanged();

                mapPaired.put(device.getName(),device);

                }
        }

        listViewPaired.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = listViewPaired.getItemAtPosition(position).toString();
                BluetoothDevice bluetoothDevice = mapPaired.get(s);
                Toast.makeText(getApplicationContext(),s+" : "+bluetoothDevice.getAddress(), Toast.LENGTH_SHORT).show();

                //connect to device here
                ClientThread clientThread = new ClientThread(bluetoothDevice);
                clientThread.start();
            }
        });
  }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch(msg.what){

                case STATE_CONNECTED:

                    BluetoothDevice device  = (BluetoothDevice) msg.obj;

                    Intent intent = new Intent(Scan.this, MainActivity.class);
                    intent.putExtra("Status", "CONNECTED");
                    intent.putExtra("Device", device.getName() );
                    startActivity(intent);

                    //Toast.makeText(getApplicationContext(), "CONNECTED To"+device.getName(), Toast.LENGTH_LONG).show();

                    break;
                case STATE_CONNECTION_FAILED:
                    Toast.makeText(getApplicationContext(), "Connection Failed: TRY AGAIN", Toast.LENGTH_LONG).show();
                    break;
            }
            return true;
        }
    });

    private class ClientThread extends Thread{
        private BluetoothDevice bluetoothDevice;
        private BluetoothSocket bluetoothSocket;

        ClientThread(BluetoothDevice bluetoothDevice) {
            this.bluetoothDevice = bluetoothDevice;

            try {
                    //UUID MY_UUID = bluetoothDevice.getUuids()[0].getUuid();
                    bluetoothSocket =bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
            }
            catch(IOException e){
                e.printStackTrace();
                Log.d("Didnt capture socket ", "inside Client thread"   );
            }
        }

        public void run(){

            try {
                bluetoothSocket.connect();

                // if connected send message to MainActivity
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                message.obj = bluetoothDevice; // also sending the socket to main activity
                handler.sendMessage(message);

            } catch (IOException e) {
                e.printStackTrace();

                Log.d("Client Socket: ","Trying fallback...");

                try {
                    bluetoothSocket =(BluetoothSocket) bluetoothDevice.getClass()
                            .getMethod("createRfcommSocket", new Class[] {int.class})
                            .invoke(bluetoothDevice,2);

                    bluetoothSocket.connect();

                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    message.obj = bluetoothDevice; // also sending the socket to main activity
                    handler.sendMessage(message);
                } catch (Exception e1){

                    Log.d("", "Couldn't establish connection");
                    e.printStackTrace();

                    // if not connected send message to MainActivity
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }


            }
        }

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

                listDiscovered.add(device.getName());
                //Log.d("devices: ", device.getName());

                arrayAdapterDiscovered.notifyDataSetChanged();

                mapDiscovered.put(device.getName(), device);
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
