package com.example.kinsense;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.telecom.Call;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {


    //components
    private Button b1;
    private TextView t1;
    private Button button_beginwork;
    private Button button_stopwork;
    private Animation rotate;
    private ImageView icanchor;
    private Chronometer timer;

    //resources
    private BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
    private BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

    //CONSTANTS
    private static final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findComponenets();

        getStatusSocket();

        setButtonClikListeners();

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }
        
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
                Intent intent = new Intent(MainActivity.this, Scan.class);
                startActivity(intent);
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

                //start gathering data from device
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
            }
        });
    }


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

    private void findComponenets() {
        t1 = findViewById(R.id.textview_connection_status); // connection status in mainActivity
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
            // Device doesn't support Bluetooth . Nothing can be done
        }else if(!bluetoothAdapter.isEnabled()){

            //directly enable without asking for permission
            bluetoothAdapter.enable();

        }else if (bluetoothAdapter.isEnabled()){
           // showPairedDevices();
           // Toast.makeText(this, "Bluetooth is already enabled!! " , Toast.LENGTH_SHORT).show();
        }
    }

/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REQUEST_ENABLE_BT){
            if(resultCode != Activity.RESULT_OK){

                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }else{
                // bluetooth has been allowed by the user in the dialog
                showPairedDevices();
            }
        }

    }
*/

}
