package com.example.kinsense;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.UUID;

public class KinService extends Service{

    private final static String TAG = KinService.class.getSimpleName();

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private String bluetoothDeviceAddress;
    private BluetoothGatt bluetoothGatt;
    private int connectionState ;
    //private BluetoothGattCharacteristic healthChar;  //test purpose


    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;


    // for Broadcast receiver to take action
    public final static String ACTION_GATT_CONNECTED =
            "com.example.kinsense.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.kinsense.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.kinsense.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.kinsense.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.kinsense.EXTRA_DATA";
    public final static String DEVICE_DOES_NOT_SUPPORT_UART =
            "com.example.kinsense.DEVICE_DOES_NOT_SUPPORT_UART"; // message to Broadcast receiver in case device
                                                                 // does not support custom UART service


    // hear rate service & char
    public static final UUID HEART_RATE_SERVICE_UUID = convertFromInteger(0x180D);  //test purpose
    public static final UUID HEART_RATE_MEASUREMENT_UUID = convertFromInteger(0x2A37); //test purpose
    //-----custom services and characteristics
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG_UUID  = convertFromInteger(0x2902) ;
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e"); //Uart Service
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");


    public boolean init(){

        if(bluetoothManager == null){
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if(bluetoothManager == null){
                Log.e(TAG, "Unable to initialize a BluetoothManager");
                return false;
            }
        }
        bluetoothAdapter = bluetoothManager.getAdapter();
        if(bluetoothAdapter == null){
            Log.e(TAG, "Unable to obtain a BluetoothAdapter");
            return false;
        }
        return true;
    }


    public static UUID convertFromInteger(int i) {

        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

    private  final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" + bluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                Log.w(TAG, "onServicesDiscovered received: " + status);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristic Read");
            if(status == BluetoothGatt.GATT_SUCCESS){
               broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
           }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristic Changed");
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    //Broadcast Update for services discovered & connection state change
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    //overload broadcastUpdate here for using characteristic in case of CharacteristicRead & Changed
    private void broadcastUpdate(final String action, BluetoothGattCharacteristic characteristic){

        final Intent intent = new Intent(action);

        if(TX_CHAR_UUID.equals(characteristic.getUuid())){
            Log.d(TAG, "found characteristic "+characteristic.getValue().toString() ) ;
            intent.putExtra(EXTRA_DATA, characteristic.getValue() ); // retreive value in MainActivity in Broadcast Receiver
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public boolean connect(final String address){
        if(bluetoothAdapter == null || address == null){
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        //Previously connected device.  Try to reconnect
        if(bluetoothGatt != null && bluetoothDeviceAddress!=null && address.equals(bluetoothDeviceAddress)) {
            Log.d(TAG, "Trying to use an EXISTING bluetoothGatt for connection instead of device.");
            if (bluetoothGatt.connect()) {
                connectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

         // connect using MAC address in method
         final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
         if(device == null) {
            Log.w(TAG, "Device not found. Unable to connect");
            return false;
         }
         bluetoothGatt = device.connectGatt(this, false, gattCallback);
         Log.d(TAG, "Trying to create a new connection.");
         bluetoothDeviceAddress = address;
         connectionState = STATE_CONNECTING;
         return true;

    }

    public void disconnect(){
        if(bluetoothAdapter == null || bluetoothGatt == null){
            Log.w(TAG, "Bluetooth Adapter not initialized");
            return;
        }
        bluetoothGatt.disconnect();
    }


    public class LocalBinder extends Binder {
        KinService getService() {
            return KinService.this;
        }
    }
    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();

    public void close() {
        if (bluetoothGatt == null) {
            return;
        }
        Log.w(TAG, "BluetoothGatt closed");
        bluetoothDeviceAddress = null;
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    /*
    public BluetoothGattCharacteristic gethealthchar(){
        BluetoothGattService heartService = bluetoothGatt.getService(HEART_RATE_SERVICE_UUID);
        healthChar = heartService.getCharacteristic(HEART_RATE_MEASUREMENT_UUID);
        return healthChar;
    }
    */

    // read data from characteristic of a service
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        bluetoothGatt.readCharacteristic(characteristic);
    }

    // write data to characteristic
    public void writeRXCharacteristic(byte[]  value){}


    // enable notify property of TX characteristic
    public void enableTXNotify(){

        BluetoothGattService RXService = bluetoothGatt.getService(RX_SERVICE_UUID);
        if(RXService == null){
            Log.w(TAG, "RX service not found. TRY RECONNECTING !!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }else
            Log.d(TAG, "RX service discovered");

        BluetoothGattCharacteristic txchar = RXService.getCharacteristic(TX_CHAR_UUID);
        if(txchar == null){
            Log.w(TAG, "TX characteristic not found! TRY RECONNECTING ");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }else
            Log.d(TAG, "TX characteristic found");


        Log.d(TAG, "Beginning to enable notify property");
        bluetoothGatt.setCharacteristicNotification(txchar, true);
        BluetoothGattDescriptor bluetoothGattDescriptor = txchar.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
        bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(bluetoothGattDescriptor);
        Log.d(TAG, "CCConfig written to descriptor");
    }
/*
    public void enableNotify(){

        BluetoothGattService HService = bluetoothGatt.getService(HEART_RATE_SERVICE_UUID);

        BluetoothGattCharacteristic characteristic = HService.getCharacteristic(HEART_RATE_MEASUREMENT_UUID);
        if(characteristic == null){
            Log.w(TAG, "characteristic not found! TRY RECONNECTING ");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }

        Log.d(TAG, "Beginning to enable notify property");

        bluetoothGatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor bluetoothGattDescriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
        bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(bluetoothGattDescriptor);
    }
*/
    public void showMessage(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }


}
