package com.example.kinsense;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

public class MainActivity extends Activity {

    //components
    private Button b1;
    private Button button_testcall;
    private TextView textView_connstatus;
    private TextView textView_showdata;
    private Button button_beginwork;
    private Button button_stopwork;
    private Animation rotate;
    private ImageView icanchor;
    private Chronometer timer;

    //resources
    private final String TAG = MainActivity.class.getSimpleName();
    private BluetoothManager bluetoothManager ;
    private BluetoothAdapter bluetoothAdapter ;
    private BluetoothDevice bluetoothDevice = null;
    private KinService kinService = null;
    private StringBuilder sb ;
    private String stringdata;
    public String dateinstance;

    //CONSTANTS or flags
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private int state = UART_PROFILE_DISCONNECTED;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static boolean FLAG_STOPPED = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"activity created");

        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        findComponenets();

        setButtonClikListeners();

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        service_init();

    }


    private void setButtonClikListeners() {


//        button_testcall.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Toast.makeText(getApplicationContext() , elapsedMillis +" ", Toast.LENGTH_LONG).show();
//            }
//       });

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
                button_stopwork.setClickable(true);
                sb = new StringBuilder();

                //capture local system time

                DateFormat dateFormat = new SimpleDateFormat("a hh:mm:ss");
                Calendar cal = Calendar.getInstance();
                Date date=cal.getTime();
                dateinstance = dateFormat.format(date);

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
                button_stopwork.setClickable(false);
                //stop timer here
                timer.stop();
                //stop gathering data from device by writing to RXcharacteristic
            }
        });
    }


    private void findComponenets() {

        //button_testcall = findViewById(R.id.button_testcall); // remove  after testing
        textView_connstatus = findViewById(R.id.textview_connection_status); // connection status in mainActivity
        //textView_showdata = findViewById(R.id.textview_showdata); // remove after testing, bottom text view to show data
        b1 = findViewById((R.id.button_bluetooth_enable));
        button_beginwork = findViewById(R.id.button_beginworkout);
        button_stopwork = findViewById(R.id.button_stopworkout);
        button_beginwork.setEnabled(false);
        button_beginwork.setText("Begin(Connect to device first)" );
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
            Intent newIntent = new Intent(MainActivity.this, DeviceControlActivity.class);
            startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
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
            int count = 0 ;

            //define for all kinService broadcast
            if(action.equals(KinService.ACTION_GATT_CONNECTED)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Uart service connected");
                        button_beginwork.setEnabled(true);
                        button_beginwork.setText("BEGIN");
                        textView_connstatus.setText("Connected to "+ bluetoothDevice.getName() );
                        state = UART_PROFILE_CONNECTED;
                    }
                });
            }

            if(action.equals(KinService.ACTION_GATT_DISCONNECTED)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    Log.d(TAG, "Uart service disconnected");
                    Toast.makeText(getApplicationContext(), "No device connected! Please connect to appropriate Device", Toast.LENGTH_LONG).show();
                    button_beginwork.setEnabled(false);
                    button_beginwork.setText("BEGIN(CONNECT TO DEVICE FIRST)");
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
                        final long elapsedMillis = SystemClock.elapsedRealtime() - timer.getBase();

                        if( !button_beginwork.isClickable() && button_stopwork.isClickable() ){
                            FLAG_STOPPED = false;
                            //start capturing data
                            //Log.d(TAG, "string builder appending");
                            sb.append(text);

                        }else if( !button_stopwork.isClickable() && button_beginwork.isClickable() && !FLAG_STOPPED){

                             if(elapsedMillis > 61000) { //check for 1 minute passed while collecting data

                                 FLAG_STOPPED = true;
                                 //stop capturing data
                                 stringdata = sb.toString();

                                 /* used to write data to a file in external storage
                                 // and later parse it to send to API
                                 writeJSONExternal( stringdata, "test" );
                                 JSONArray jsonArray = getJsonExternalParsed();
                                 writeJSONExternal( jsonArray.toString(), "testParsed" );
                                 */

                                 //make API call
                                 CallAPI callAPI = new CallAPI(MainActivity.this, stringdata);  // sending context to test with JSON data in assets
                                 callAPI.execute();  // to run the doInBackground method of AsyncTask

                                 Log.d(TAG, "final string data length: " + stringdata.length());
                             }else{
                                 Toast.makeText(getApplicationContext(), "Please walk atleast 1 minute to get enough data!!", Toast.LENGTH_SHORT).show();
                             }
                        }
                        //textView_showdata.setText(text);
                    }
                });
            }

            if(action.equals(KinService.DEVICE_DOES_NOT_SUPPORT_UART)){
                Log.e(TAG, "Device does not support Uart service.");
                kinService.disconnect();
            }


        }
    };

    /*
    private void writeJSONExternal(String json, String filename) {

        String root = getExternalFilesDir(null).getAbsolutePath();
        File file = new File(root + "/"+filename+".json");

        FileOutputStream fos ;

        try {
            fos = new FileOutputStream(file);//context.openFileOutput("test.txt", Context.MODE_PRIVATE);
            fos.write( json.getBytes() );
            Log.d(TAG, "file written to "+ getExternalFilesDir(null) + "/"+filename+".json");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e1){
            e1.printStackTrace();
        }

    }

    public JSONArray getJsonExternalParsed(){
        JSONArray jsonArray = new JSONArray();

        String root = getExternalFilesDir(null).getAbsolutePath();
        File file = new File(root + "/test.json");

        SimpleDateFormat dateFormat = new SimpleDateFormat("a hh:mm:ss");
        Date date = null;
        try {
            date = dateFormat.parse(dateinstance);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int count = 1;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String s;
            while( (s = br.readLine()) != null ){
                int len = s.length();

                if ( len > 72 && len <80 ){

                    if(count > 32) {
                        count = 1;
                        date.setTime( date.getTime() + 1000);
                    }

                    JSONObject json = new JSONObject(s);
                    json.put("index", count);
                    json.put("Time", dateFormat.format(date) );
                    count += 1;

                    //  add to JsonArray
                    jsonArray.put(json);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e1){
            e1.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonArray;
    }
    */

    //Kinservice connected/disconnected
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            kinService = ((KinService.LocalBinder) service).getService();
            Log.d(TAG, "kinService Connected. Service Connection Initialized");
            if(!kinService.init()) {
                Log.e(TAG, "Unable to initialize ble.");
                finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "kinService disConnected");
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
        Log.d(TAG, "activity resumed and Kinservice: "+ kinService );
        if(!bluetoothAdapter.isEnabled()){

            //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "activity destroyed");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);

        if(serviceConnection != null)
            unbindService(serviceConnection);
        kinService.stopSelf();
        kinService = null ;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "activity pausued");
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "activity stopped");

    }


}
