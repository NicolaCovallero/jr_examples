package com.example.dado.bluetooth;

import android.bluetooth.BluetoothAdapter;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;

public class MainActivity extends AppCompatActivity {

    protected Button startConnectionPi = null;
    protected Button stopConnectionPi = null;
    protected Button communicationPi = null;
    protected Handler mHandler, textHandler;
    protected TextView textView = null;

    private int max_discovering_time = 10000; // 10 sec
    private BluetoothConnection blue_connection = new BluetoothConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startConnectionPi = (Button) findViewById(R.id.button_start);
        startConnectionPi.setEnabled(true);
        startConnectionPi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //new RingDialog().execute(v);
                final RingDialog rd = new RingDialog();
                rd.launchRingDialog(v);
                addText("Connecting...");
            }
        });

        stopConnectionPi = (Button) findViewById(R.id.button_stop);
        stopConnectionPi.setEnabled(false);
        stopConnectionPi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blue_connection.closeConnection();
                stopConnectionPi.setEnabled(false);
                communicationPi.setEnabled(false);
            }
        });

        communicationPi = (Button) findViewById(R.id.button_communication);
        communicationPi.setEnabled(false);
        communicationPi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = "che bello :)";
                blue_connection.SimpleTestCommunication(msg,textHandler);
            }
        });

        if (!blue_connection.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Bluetooth not activated", Toast.LENGTH_SHORT).show();

            Intent on = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(on, 0);

        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth activated", Toast.LENGTH_SHORT).show();
        }

        textView = (TextView) findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        addText("you have to run rfcomm-server2.py in the raspberry");
        textHandler = new Handler(Looper.getMainLooper()) {
            /*
             * handleMessage() defines the operations to perform when
             * the Handler receives a new Message to process.
             */
            @Override
            public void handleMessage(Message inputMessage) {
                String msg = (String) inputMessage.obj;
                addText(msg);

            }
        };
    }



    private class RingDialog {
        protected int state;

        public RingDialog(){
             state = 0;
            /**
             * This handler is executed in the UI Thread when it receives a Message
             * which could have some information, in this case this handler manage to enable
             * STOP and COMMUNICATION buttons when it receives a message (which is send when the
             * connection has been established or time out happened), and check if effectively the
             * connection has been established (inputMessage.what == 1).
             * NOTE: This handler can de deefined in whatever part/subclass of the code (by the way it is executed
             * in the main Thread), but it is better to declear it
             * as member of the mainActivity.
             */
            mHandler = new Handler(Looper.getMainLooper()) {
                    /*
                     * handleMessage() defines the operations to perform when
                     * the Handler receives a new Message to process.
                     */
                    @Override
                    public void handleMessage(Message inputMessage) {
                        if (inputMessage.what == 1) {
                            Log.d("handleMessage", "enabling buttons");
                            stopConnectionPi.setEnabled(true);
                            communicationPi.setEnabled(true);
                            addText("Connected with Johnny! :)");

                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Impossible to connect to the robot, try it again.", Toast.LENGTH_SHORT).show();
                            addText("Impossible to connect to the robot, try it again.");
                        }
                    }
                };
        }

        /**
         * It shows a ring dialong which spins until timeout or the connection has been established
         * @param view
         */
        public void launchRingDialog(View view) {
            final ProgressDialog ringProgressDialog = ProgressDialog.show(MainActivity.this, "Please wait ...", "Searching for Johnny Robot ... where is he? ", true);
            ringProgressDialog.setCancelable(false); // prevent to close when touched outside the dialog box
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        blue_connection.DiscoverDevices();
                        blue_connection.ConnectToRobot();

                        // continue until the connection has been established
                        int counter = 0;
                        while (blue_connection.connectionState() == 3 && (counter < max_discovering_time)) {
                            Thread.sleep(100);
                            counter += 100;
                        }
                        if ((counter >= max_discovering_time)) {
                            state = 0;
                            Log.d("Maximum discoverying time exceeded", ".....");
                            addText("Maximum discoverying time exceeded...");
                        } else if (blue_connection.connectionState() == 1) {
                            state = 1;
                        }

                    } catch (Exception e) {
                        Log.d("RING DIALOG EXECEPTION", e.getMessage());
                    }

                    ringProgressDialog.dismiss();

                    /**
                     * Prepare the message for the mHandler
                     */
                    Message completeMsg = mHandler.obtainMessage(state);
                    /**
                     * Sending the message to mHandler
                      */
                    completeMsg.sendToTarget();
                }
            }).start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
//        if (requestCode == 0) {
//            // Make sure the request was successful
//            if (resultCode == RESULT_OK) {
//                //paredDevices();
//                Discovering();
//            }
//            else{
//                Toast.makeText(getApplicationContext(), "Bluetooth refused activation", Toast.LENGTH_SHORT).show();
//            }
//        }
    }

    /**
     * Add a new line of text to the textView
     * @param text
     * @return
     */
    private Void addText(String text){
        String text_view_text=textView.getText().toString();
        StringBuffer sb=new StringBuffer(text_view_text);
        sb.append(text + "\n");
        textView.setText(sb.toString());
        return null;
    }




}
