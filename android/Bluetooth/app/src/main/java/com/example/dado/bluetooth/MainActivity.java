package com.example.dado.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter ba;
    private Set<BluetoothDevice> devices;
    private BluetoothDevice piDevice = null;//Raspberrypi bluetooth device
    private ConnectThread newConnection = null;
    private CommunicationThread currentCommunication = null;

    private Button startConnectionPi = null;
    private Button stopConnectionPi = null;
    private Button communicationPi = null;

    final String MAC = "00:15:83:E8:49:2D";//CHANGE STRING WHEN CHANGE RASPBERRYPI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ba = BluetoothAdapter.getDefaultAdapter();


        startConnectionPi = (Button) findViewById(R.id.button_start);
        startConnectionPi.setEnabled(false);
        startConnectionPi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newConnection = new ConnectThread(piDevice);
                newConnection.run();
                stopConnectionPi.setEnabled(true);
            }
        });

        stopConnectionPi = (Button) findViewById(R.id.button_stop);
        stopConnectionPi.setEnabled(false);
        stopConnectionPi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newConnection.cancel();
                currentCommunication.cancel();
                communicationPi.setEnabled(false);
                stopConnectionPi.setEnabled(false);
            }
        });

        communicationPi = (Button) findViewById(R.id.button_communication);
        communicationPi.setEnabled(false);
        communicationPi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("COMMUNICAZIONE", "Comunicazione attiva");
                /*currentConnection.write("ciao".getBytes());
                Log.d("COMMUNICAZIONE", "Inviata parola");*/
                Log.d("COMMUNICAZIONE", "Comunicazione avviata");
                new Thread(currentCommunication).start();
            }
        });

        if(!ba.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Bluetooth not activated", Toast.LENGTH_SHORT).show();

            Intent on = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(on, 0);

        }else{
            Toast.makeText(getApplicationContext(), "Bluetooth activated", Toast.LENGTH_SHORT).show();

            //paredDevices();
            Discovering();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 0) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                //paredDevices();
                Discovering();
            }
            else{
                Toast.makeText(getApplicationContext(), "Bluetooth refused activation", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void paredDevices(){

        String list = "";
        String dAddress = "";

        devices = ba.getBondedDevices();

        //Log.d("Passaggio1","Passato");

        for(BluetoothDevice bd: devices){
            dAddress = bd.getAddress();
            if(dAddress.equals(MAC))
                piDevice = bd;
            list+= bd.getName() + ";";
        }

        //Log.d("Passaggio2","Passato");

        //Toast.makeText(getApplicationContext(), list, Toast.LENGTH_SHORT).show();
        Log.d("Devices list", list);

        //Log.d("Passaggio3","Passato");
    }

    private void Discovering(){

        paredDevices();

        if(piDevice != null){
            Log.d("PASSAGGIO", "Found raspberry");

            //newConnection = new ConnectThread(piDevice);
            startConnectionPi.setEnabled(true);
            Log.d("Connecting", "Connecting with Raspberry");
        }
        else {

            boolean discovery = ba.startDiscovery();
            Log.d("Discovering", discovery + "");

            final BroadcastReceiver dReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    String action = intent.getAction();
                    String dName = "";
                    String dAddress = "";

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

                            //newConnection = new ConnectThread(piDevice);
                            startConnectionPi.setEnabled(true);
                            Log.d("Connecting", "Connessione con raspberrypi");
                        }
                    }

                }
            };

            // Register the BroadcastReceiver
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(dReceiver, filter); // Don't forget to unregister during onDestroy
        }

    }

    private class ConnectThread extends Thread{
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
                //tmp = device.createRfcommSocketToServiceRecord(myUUID);
                tmp = device.createInsecureRfcommSocketToServiceRecord(myUUID);
            } catch (IOException e) { }
            ddSocket = tmp;
        }

        public void run() {
            Log.d("PASSAGGIO", "Eseguito il run");
            // Cancel discovery because it will slow down the connection
            ba.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                Log.d("PASSAGGIO", "Partita la connessione");
                ddSocket.connect();
                Log.d("PASSAGGIO", "Eseguito la connessione");
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                Log.d("PASSAGGIO", "Non riesce a connettersi");
                try {
                    ddSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            currentCommunication = new CommunicationThread(ddSocket);
            communicationPi.setEnabled(true);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                ddSocket.close();
                Log.d("CANCEL", "Cancellazione connessione");
            } catch (IOException e) { }
        }
    }

    private class CommunicationThread extends Thread {
        private final BluetoothSocket ddSocket;
        private final InputStream ddInStream;
        private final OutputStream ddOutStream;

        public CommunicationThread(BluetoothSocket socket) {
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

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            String counter = "0";
            int counterTmp = 0;

            Log.d("RUNNED","RUNNED");

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    Log.d("INVIATO", counter);
                    currentCommunication.write(counter.getBytes());
                    // Read from the InputStream
                    bytes = ddInStream.read(buffer);
                    //String readMessage = new String(buffer, 0, bytes);
                    counter = new String(buffer, 0, bytes);
                    Log.d("RICEVUTO", counter);
                    counterTmp = Integer.parseInt(counter) + 1;
                    counter = counterTmp + "";
                } catch (IOException e) {
                    Log.d("Exception",e.toString());
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                ddOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                Log.d("CANCEL", "Cancellazione connessione");
                ddSocket.close();
            } catch (IOException e) { }
        }
    }

}
