package com.example.dado.bluetooth;

import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
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
    protected ConnectionThread connection = null;

    final protected String MAC = "B8:27:EB:20:53:7F";//"00:15:83:E8:49:2D";//CHANGE STRING WHEN CHANGE RASPBERRYPI
    final protected String ROBOT_UUID = "94f39d29-7d6d-437d-973b-fba39e49d4ea";


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
    }

    /**
     * Class to establish connection with the robot. This is implemented as a Thread.
     */
    private class EstablishConnectionThread extends Thread{
        private BluetoothSocket ddSocket = null;
        private BluetoothDevice ddDevice = null;

        public EstablishConnectionThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            ddDevice = device;

            UUID myUUID = UUID.fromString(ROBOT_UUID);

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                //tmp = device.createRfcommSocketToServiceRecord(myUUID);
                tmp = device.createInsecureRfcommSocketToServiceRecord(myUUID);
            } catch (IOException e) {
            }
            ddSocket = tmp;

            // this is important to put it inside this initializer.
            // GENERAL NOTE: is we use a RINGDIAGLOG THREAD (see main activity)
            // it is possible that we enter the while loop before the thread started effectively
            // to run EstablishConnectionThread, thus connection_state could be still set to NOT_CONNECTED.
            connection_state = CONNECTING;

        }

        public void run() {
            Log.d("PASSAGGIO", "Eseguito il run");
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

            // Once the raspberry has been found initialize the connection object
            connection = new ConnectionThread(ddSocket);

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
     * Simple class which sends repeatdly a message and read it back. This is implemented as a Thread.
     */
    private class ConnectionThread extends Thread {
        private final BluetoothSocket ddSocket;
        private final InputStream ddInStream;
        private final OutputStream ddOutStream;
        private String msg = "DEFAULT MESSAGE";
        private Handler msgHandler;

        public ConnectionThread(BluetoothSocket socket) {
            ddSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            ddInStream = tmpIn;
            ddOutStream = tmpOut;

        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    if(ddSocket.isConnected()) {
                        // Read from the InputStream
                        Message msg_ = Message.obtain();
                        msg_.setTarget(msgHandler);
                        if(msg_.getTarget() != null) {
                            msg_.obj = "Sending:" + msg;
                            msg_.sendToTarget();
                        }
                        write(msg.getBytes());

                        bytes = ddInStream.read(buffer);
                        String readMessage = new String(buffer, 0, bytes);
                        Log.d("RECEIVED", readMessage);

                        msg_ = Message.obtain();
                        msg_.setTarget(msgHandler);
                        if(msg_.getTarget() != null) {
                            msg_.obj = "Receiving:" + msg;
                            msg_.sendToTarget();
                        }

                        // Send the obtained bytes to the Log
                    /*mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();*/
                    }
                    //else
                    //{
                    //    Log.d("COMMUNICATION","Attempting to communicate but the socket is not connected");
                    //}
                } catch (IOException e) {
                    Log.d("EXCEPTION",e.getMessage());
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */

        /**
         * Send bytes to the raspberry
         * @param bytes
         */
        public void write(byte[] bytes) {
            try {
                ddOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /**
         *         set the handler that updates the textView
        */
        public void setHandler(Handler handler){
            msgHandler = handler;
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                Log.d("CANCEL", "Closing the connection");
                ddSocket.close();
            } catch (IOException e) {
                Log.d("CANCEL", "Impossible closing the connection");
            }
        }

        /**
         *
          * @param msg_ message to send
         */
        public void setMsg(String msg_)
        {
            msg = msg_;
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
            Log.d("CONNECTTOROBOT:","Discovering device");
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
        establishConnection.cancel();
        connection.cancel();
        communication_state = NOT_CONNECTED;
    }

    /**
     * Return true if the bluethooth connection is effectively enabled.
      * @return
     */
    public boolean isEnabled(){
        return ba.isEnabled();
    }


    /**
    * This functions just call the CommunicationThread which is a infinite loop which send the message
    * in "msg_"
     */
    public void SimpleTestCommunication(String msg_,Handler handler) {
        if (connection_state == CONNECTED) {
            connection.setHandler(handler);
            connection.setMsg(msg_);
            connection.start();
        }else{
            Log.d("SimpleTestCommunication","Not connection established yet");
        }
    }

}
