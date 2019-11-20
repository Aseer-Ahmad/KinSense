package com.example.kinsense;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class
MainActivity extends AppCompatActivity {


    //components
    private Button b1;
    private TextView t1;
    private TextView textView_showdata;
    private Button button_beginwork;
    private Button button_stopwork;
    private Animation rotate;
    private ImageView icanchor;
    private Chronometer timer;

    //resources
    private final String TAG = MainActivity.class.getSimpleName();
    private BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
    private BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
    private BluetoothDevice bluetoothDevice = null;
    private KinService kinService = null;

    //CONSTANTS or flags
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private int state = UART_PROFILE_DISCONNECTED;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findComponenets();

        //getStatusSocket();

        setButtonClikListeners();

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        service_init();


        //CallAPI class TESTING
        //later after testing set JSON data in constructor
        //execute the async method
        //getResponse and send it to a new activity
        //CallAPI callAPI = new CallAPI(this);  // sending context to test with JSON data in assets
        //callAPI.execute();  // to run the doInBackground method of AsyncTask

    }


    private void setButtonClikListeners() {

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // bluetooth enable button

                enableBluetooth();
            }
        });

        button_beginwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                icanchor.startAnimation(rotate);
                button_stopwork.animate().alpha(1).translationY(-80).setDuration(400).start();
                button_beginwork.animate().alpha(0).setDuration(400).start();
                button_beginwork.setClickable(false);
                //set timer here
                timer.setBase(SystemClock.elapsedRealtime());
                timer.start();

                //start gathering data from device by writing to RXcharacteristic
            }
        });

        button_stopwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                icanchor.clearAnimation();
                button_beginwork.animate().alpha(1).setDuration(400).start();
                button_stopwork.animate().alpha(0).translationY(80).setDuration(400).start();
                button_beginwork.setClickable(true);
                //stop timer here
                timer.stop();

                //stop gathering data from device by writing to RXcharacteristic
            }
        });
    }




    private void findComponenets() {
        t1 = findViewById(R.id.textview_connection_status); // connection status in mainActivity
        textView_showdata = findViewById(R.id.textview_showdata); // bottom text view to show data
        b1 = findViewById((R.id.button_bluetooth_enable));
        button_beginwork = findViewById(R.id.button_beginworkout);
        button_stopwork = findViewById(R.id.button_stopworkout);
        button_stopwork.setAlpha(0);
        rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
        icanchor = findViewById(R.id.icanchor);
        timer = findViewById(R.id.chronometer_timer);
    }


    private void enableBluetooth() {

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth .
            Log.e(TAG, "Device dosen't support Bluetooth. Can't help :(");
        }else if(!bluetoothAdapter.isEnabled()){

            //request to enable permission
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        }else if (bluetoothAdapter.isEnabled()){
           // check if device connected or else to DeviceControlActivity

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REQUEST_ENABLE_BT){
            if(resultCode != Activity.RESULT_OK){

                Toast.makeText(this, "Cannot connect to Device. Bluetooth is not enabled! Try Again", Toast.LENGTH_SHORT).show();
            }else{
                // bluetooth has been allowed by the user in the dialog
                // got to DeviceControlActivity for result
                Intent newIntent = new Intent(MainActivity.this, DeviceControlActivity.class);
                startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
            }
        }else if (requestCode == REQUEST_SELECT_DEVICE){
            if(resultCode == Activity.RESULT_OK){
                String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);

                // connect to device using KinService .connect()
                kinService.connect(deviceAddress);

            }
        }
    }

    //use Broadcast Receiver to capture broadcast from Kinservice
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //define for all kinService broadcast
            if(action.equals(KinService.ACTION_GATT_CONNECTED)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Uart service connected");
                        button_beginwork.setEnabled(true);
                        state = UART_PROFILE_CONNECTED;
                    }
                });
            }

            if(action.equals(KinService.ACTION_GATT_DISCONNECTED)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    Log.d(TAG, "Uart service disconnected");
                    button_beginwork.setEnabled(false);
                    state = UART_PROFILE_DISCONNECTED;
                    }
                });
            }

            if(action.equals(KinService.ACTION_GATT_SERVICES_DISCOVERED)){
                // enable TXnotification
                kinService.enableTXNotify();
            }

            if(action.equals(KinService.ACTION_DATA_AVAILABLE)){
                final byte[] txValue = intent.getByteArrayExtra(KinService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String text = new String(txValue, StandardCharsets.UTF_8);
                        textView_showdata.setText(text);
                    }
                });
            }

            if(action.equals(KinService.DEVICE_DOES_NOT_SUPPORT_UART)){
                Log.e(TAG, "Device does not support Uart service.");
                kinService.disconnect();
            }



        }
    };

    //Kinservice connected/disconnected
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            kinService = ((KinService .LocalBinder) service).getService();
            Log.d(TAG, "in on Service Connected");
            if(!kinService.init()) {
                Log.e(TAG, "Unable to initialize ble");
                finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            kinService = null;
        }
    };

    private void service_init() {
        Intent bindIntent = new Intent(this, KinService.class);
        bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver( broadcastReceiver , makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(KinService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(KinService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(KinService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(KinService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(KinService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!bluetoothAdapter.isEnabled()){

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        }
    }

    /*
    private void getStatusSocket() {
        //Get data/status & socket from Scan.class intent
        Intent intent = getIntent();
        String status = intent.getStringExtra("Status");
        String devicename = intent.getStringExtra("Device");
        if(status !=null)
            t1.setText("CONNECTED to "+devicename);


        if(t1.getText().equals("NOT CONNECTED") ) {
            button_beginwork.setEnabled(false);
            button_beginwork.setText("BEGIN (first connect to device)");
        }else{
            button_beginwork.setEnabled(true);
            button_beginwork.setText("BEGIN");
        }
    }
    */

}
