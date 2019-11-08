package com.example.kinsense;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
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
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    //CONSTANTS
    private static final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findComponenets();
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                    //set timer here
                    timer.setBase(SystemClock.elapsedRealtime());
                    timer.start();
            }
        });

        button_stopwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                icanchor.clearAnimation();
                button_beginwork.animate().alpha(1).setDuration(400).start();
                button_stopwork.animate().alpha(0).translationY(80).setDuration(400).start();
                //stop timer here
                timer.stop();
            }
        });

    }

    private void findComponenets() {
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
