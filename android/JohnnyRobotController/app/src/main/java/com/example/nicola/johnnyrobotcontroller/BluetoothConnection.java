package com.example.nicola.johnnyrobotcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by nicola on 22/12/16.
 */
public class BluetoothConnection {
    protected BluetoothAdapter ba;

    protected Set<BluetoothDevice> devices;
    protected BluetoothDevice piDevice = null;//Raspberrypi bluetooth device
    protected EstablishConnectionThread establishConnection = null;
    //protected ConnectionThread connection = null;
    protected BluetoothSocket ddSocket;

    protected String MAC = "00:15:83:E8:49:2D";//"00:15:83:E8:49:2D";//CHANGE STRING WHEN CHANGE RASPBERRYPI
    protected String SERVICE_UUID = "94f39d29-7d6d-437d-973b-fba39e49d4ed";
    protected String socket_description; // description of the socket

    protected int CONNECTED = 1;
    protected int NOT_CONNECTED = 2;
    protected int CONNECTING = 3;
    protected int connection_state; // to specify the state of the connection (CONNECTED OR NOT_CONNECTED)
    protected int communication_state; // variable to specify it is it sending, receiving, or doing nothing

    public BluetoothConnection()
    {
        Log.d("BluetoothConnection"," called");
        ba = BluetoothAdapter.getDefaultAdapter();
        connection_state = NOT_CONNECTED;
        socket_description = "default";
    }

    /**
     * Class to establish connection with the robot. This is implemented as a Thread.
     */
    private class EstablishConnectionThread extends Thread {

        public EstablishConnectionThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;

            UUID myUUID = UUID.fromString(SERVICE_UUID);
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                //tmp = device.createRfcommSocketToServiceRecord(myUUID);
                tmp = device.createInsecureRfcommSocketToServiceRecord(myUUID);
            } catch (IOException e) {
            }
            ddSocket = tmp;

            // this is important to put it inside this initializer.
            connection_state = CONNECTING;

        }

        public void run() {
            //Log.d("PASSAGGIO", "Eseguito il run");
            ba.cancelDiscovery();
            connection_state = CONNECTING;

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                ddSocket.connect();
                connection_state = CONNECTED;

            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    connection_state = NOT_CONNECTED;
                    ddSocket.close();
                } catch (IOException closeException) { }
                return;
            }


        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                connection_state = NOT_CONNECTED;
                ddSocket.close();

            } catch (IOException e) { }
        }
    }

    /**
     * Check the pared devices
     */
    private void paredDevices(){

        String list = "";
        String dAddress = "";

        devices = ba.getBondedDevices();

        for(BluetoothDevice bd: devices){
            dAddress = bd.getAddress();
            if(dAddress.equals(MAC))
                piDevice = bd;
                Log.d("PARING:","Found raspberry in the pared devices list");
            list+= bd.getName() + ";";
        }

        Log.d("Devices list", list);

    }

    /**
     * Connect to the robot, if this is not already connected let's discover it.
     */
    public void ConnectToRobot()
    {
        paredDevices();

        if (piDevice == null) {
            Log.d("CONNECT TO ROBOT:","Discovering device");
            DiscoverDevices();
        }

        if (connection_state != CONNECTED) {
            //Log.d("Connecting", "Found raspberry");
            establishConnection = new EstablishConnectionThread(piDevice);
            Log.d("Connecting", "Connecting with Raspberry");
            establishConnection.start();
        } else {
            Log.d("Connecting", "Already Connected");
        }


    }

    /**
     * Discover devices, this is also called by ConnectToRobot if it has been not called before, or the
     * raspberry was not found.
     */
    public void DiscoverDevices() {


        boolean discovery = ba.startDiscovery();
        Log.d("Discovering devices", discovery + "");

        final BroadcastReceiver dReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                String dName = "";
                String dAddress = "";
                Log.d("PASSAGGIO", "Discovering");

                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice tmpDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    Log.d("Device", tmpDevice.getName() + " - " + tmpDevice.getAddress());
                    dName = tmpDevice.getName();
                    dAddress = tmpDevice.getAddress();

                    if (dAddress.equals(MAC)) {
                        Log.d("PASSAGGIO", "Trovato raspberry");
                        piDevice = tmpDevice;

                        //establishConnection = new EstablishConnectionThread(piDevice);
//                            startConnectionPi.setEnabled(true);
                        Log.d("Connecting", "Connessione con raspberrypi");
                    }
                }

            }
        };

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //registerReceiver(dReceiver, filter); // Don't forget to unregister during onDestroy

    }


    /**
     * Obtain the connection state
     * CONNECTED = 1 It is connected to the raspberry
     * NOT_CONNECTED = 2 It is NOT connected to the raspberry
     * CONNECTING = 3 It is connecting, executing EstablishConnectionThread
     * @return
     */
    public int connectionState(){
        return connection_state;
    }

    /**
     * Close the connection
     */
    public void closeConnection(){
        if(connection_state == CONNECTED) {
            try {
                connection_state = NOT_CONNECTED;
                ddSocket.close();
            } catch (IOException closeException) { }
        }
         connection_state = NOT_CONNECTED;
    }

    /**
     * Return true if the bluethooth connection is effectively enabled.
      * @return
     */
    public boolean isEnabled(){
        return ba.isEnabled();
    }

    /**
     * Send data to the server in String format. It might happen that the buffer is overloaded and the
     * other device could not read as fast as the buffer is loaded resulting in a wrong data received by
     * by the other device. See sendStringSync().
     * @param msg_
     */
    public void sendString(String msg_) {
        if (connection_state == CONNECTED) {
            try {
                Log.d("Socket"+socket_description,"Sending"+msg_);
               ddSocket.getOutputStream().write(msg_.getBytes());
            } catch (IOException e) {
                Log.d("SendString exception","");
                communication_state = NOT_CONNECTED;
            }
        }else{
           // Log.d("BluetoothConnection","Not connection established yet");
        }
    }

    /**
     * Send the data in String format and wait to receive a message from the server before to return.
     * @param msg_ Data
     */
    public void sendStringSync(String msg_){
        if (connection_state == CONNECTED) {
            try {
                //Log.d("Socket"+socket_description,"Sending"+msg_);
                ddSocket.getOutputStream().write(msg_.getBytes());

                // read message sending by the server
                byte[] buffer = new byte[1024];  // buffer store for the stream
                int bytes; // bytes returned from read()
                bytes = ddSocket.getInputStream().read(buffer);
                String readMessage = new String(buffer, 0, bytes);
                //Log.d("Received:",readMessage);

            } catch (IOException e) {
                Log.d("SendString exception","");
                communication_state = NOT_CONNECTED;
            }
        }else{
            //Log.d("BluetoothConnection","Not connection established yet");
        }
    }

    public void setMACAddress(String addr){
        MAC = addr;
    }

    public void setServiceUUID(String uuid){
        SERVICE_UUID = uuid;
    }

    public String getMACAddress(){
        return MAC;
    }

    public String getSERVICE_UUID(){
        return SERVICE_UUID;
    }

    public void setSocketDescription(String desc){
        socket_description = desc;
    }

    public String getSocketDescription(){
        return socket_description;
    }
}
