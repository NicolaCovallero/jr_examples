package com.example.dado.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by DADO on 12/11/2016.
 */

class ConnectThread extends Thread{
    private BluetoothSocket ddSocket = null;
    private BluetoothDevice ddDevice = null;

    public ConnectThread(BluetoothDevice device) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        ddDevice = device;

        UUID myUUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(myUUID);
        } catch (IOException e) { }
        ddSocket = tmp;
    }
}
