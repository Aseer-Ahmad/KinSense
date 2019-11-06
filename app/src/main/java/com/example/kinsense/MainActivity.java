package com.example.kinsense;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {


    //components
    private Button b1;
    private TextView t1;

    //resources
    private BluetoothAdapter bluetoothAdapter = null;

    //CONSTANTS
    private static final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        b1 = findViewById((R.id.button_bluetooth_enable));
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                enableBluetooth();
                Intent intent = new Intent(MainActivity.this, Scan.class);
                startActivity(intent);
            }
        });


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

    private void showPairedDevices() {

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        int totaldevices = pairedDevices.size();

        if (totaldevices > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                // for displaying information
            }
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
