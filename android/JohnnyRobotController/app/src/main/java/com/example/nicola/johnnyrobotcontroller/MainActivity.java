package com.example.nicola.johnnyrobotcontroller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Canvas;
import android.widget.Toast;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    private Bitmap js_motors, js_camera;
    private Button button_connect, button_start;
    private CheckBox cb_bluetooth, cb_wifi;
    private ImageView imageView;
    private TextView textView_terminal;
    private boolean connection_wifi = true; // false when the connection is via bluetooth
    private Handler textHandler;
    protected Handler terminalHandler;
    private boolean connection;

    private int max_discovering_time = 20000; // 10 sec
    private BluetoothConnection motors_blue_connection, camera_motors_blue_connection;// bluetooth connection

    private final int CONNECTION_PORT = 2525;
    private final int DRIVING_PORT = 2526;
    private final int CAMERA_DRIVING_PORT = 2529;
    private WifiConnection connection_socket;

    final private String MAC = "00:15:83:E8:49:2D";//"00:15:83:E8:49:2D";//CHANGE STRING WHEN CHANGE RASPBERRYPI
    final private String DRIVING_SERVICE_UUID = "94f39d29-7d6d-437d-973b-fba39e49d4ed";
    final private String DRIVING_CAMERA_SERVICE_UUID = "94f39d29-7d6d-437d-973b-fba39e49d4ec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connection = false;

        motors_blue_connection = new BluetoothConnection();
        motors_blue_connection.setMACAddress(MAC);
        motors_blue_connection.setServiceUUID(DRIVING_SERVICE_UUID);
        motors_blue_connection.setSocketDescription("wheels motors");

        camera_motors_blue_connection = new BluetoothConnection();
        camera_motors_blue_connection.setMACAddress(MAC);
        camera_motors_blue_connection.setServiceUUID(DRIVING_CAMERA_SERVICE_UUID);
        camera_motors_blue_connection.setSocketDescription("camera motors");

        button_connect = (Button) findViewById(R.id.button_connect);
        button_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(! connection_wifi)
                {
                    motors_blue_connection.closeConnection();
                    camera_motors_blue_connection.closeConnection();

                    final RingDialog rd = new RingDialog();
                    rd.launchRingDialog(v);
                    writeTerminal("Connecting via Bluetooth...");
                }
                else{
                    // thread to send and receive back a data
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            connection_socket.connect(CONNECTION_PORT); // it is better to put it into a runnable
                            connection_socket.sendStringData("connected");
                            String msg_rcv = connection_socket.receiveStringData();
                            String[] data_rcv = msg_rcv.split("/");
                            if (  data_rcv[0].equals("jr_data") ){
                                sendTerminalHandlerMsg("Connected with Johnny Robot via WIFI! :)");
                            }
                        }
                    }).start();
                }
            }
        });

        button_start = (Button) findViewById(R.id.button_start);
        textView_terminal = (TextView) findViewById(R.id.textView_terminal);
        textView_terminal.setMovementMethod(new ScrollingMovementMethod());
        writeTerminal("Welcome, we are happy you are using our app to control our robot." +
                " We hope you will enjoy it :)");
        textHandler = new Handler(Looper.getMainLooper()) {
            /*
             * handleMessage() defines the operations to perform when
             * the Handler receives a new Message to process.
             */
            @Override
            public void handleMessage(Message inputMessage) {
                String msg = (String) inputMessage.obj;
                writeTerminal(msg);

            }
        };

        cb_bluetooth = (CheckBox) findViewById(R.id.checkBox_bluetooth);
        cb_wifi = (CheckBox) findViewById(R.id.checkBox_wifi);
        // set default checkbox
        if (connection_wifi) {
            cb_bluetooth.setChecked(false);
            cb_wifi.setChecked(true);
            connection_socket = new WifiConnection();
        }
        else{
            cb_bluetooth.setChecked(true);
            cb_wifi.setChecked(false);
        }
        cb_bluetooth.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (cb_bluetooth.isChecked()) {
                    if (!motors_blue_connection.isEnabled()) {
                        Toast.makeText(getApplicationContext(), "Bluetooth not activated", Toast.LENGTH_SHORT).show();
                        Intent on = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(on, 0);
                    } else {
                        Toast.makeText(getApplicationContext(), "Bluetooth activated", Toast.LENGTH_SHORT).show();
                    }

                    connection_wifi = false;
                    cb_bluetooth.setChecked(true);
                    cb_wifi.setChecked(false);
                    writeTerminal("Chosen bluetooth communication, good choice bro! ;)");


                }
                else {
                    BluetoothAdapter.getDefaultAdapter().disable();
                    connection_wifi = true;
                    cb_bluetooth.setChecked(false);
                    cb_wifi.setChecked(true);
                    writeTerminal("Chosen wifi connection. We are sorry but the app still cannot"+
                            " communicate with johnny via wifi :(");
                }
            }
        });
        cb_wifi.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (cb_wifi.isChecked()) {
                    BluetoothAdapter.getDefaultAdapter().disable();
                    connection_wifi = true;
                    cb_bluetooth.setChecked(false);
                    cb_wifi.setChecked(true);
                    connection_socket = new WifiConnection();

                    writeTerminal("Chosen wifi communication, good choice bro! ;)");
                }
                else {
                    if (!motors_blue_connection.isEnabled()) {
                        Toast.makeText(getApplicationContext(), "Bluetooth not activated", Toast.LENGTH_SHORT).show();
                        Intent on = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(on, 0);
                    } else {
                        Toast.makeText(getApplicationContext(), "Bluetooth activated", Toast.LENGTH_SHORT).show();
                    }

                    connection_wifi = false;
                    cb_bluetooth.setChecked(true);
                    cb_wifi.setChecked(false);
                    writeTerminal("Chosen bluetooth communication, good choice bro! ;)");
                }
            }
        });
        button_start.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                startControllerActivity();
            }


        });



        js_motors = BitmapFactory.decodeResource(getResources() , R.drawable.joystick);
        //imageView = (ImageView) findViewById(R.id.imageView);

        //Create a new image bitmap and attach a brand new canvas to it
        Bitmap tempBitmap = Bitmap.createBitmap(js_motors.getWidth()*3, js_motors.getHeight()*2, Bitmap.Config.ARGB_8888);

        //Matrix m = new Matrix();
        //m.setScale((float)  js_motors.getWidth() *3/ js_motors.getWidth(), (float) js_motors.getHeight() * 2 / js_motors.getHeight());

        //Canvas tempCanvas = new Canvas(tempBitmap);

        //Draw the image bitmap into the cavas
        //tempCanvas.drawBitmap(js_motors, m, null);


        //imageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

        // just for testing
        /*
        //imageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                writeTerminal("You clicked the imageView ");
            }
        });
        */

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
        terminalHandler = new Handler(Looper.getMainLooper()) {
            /*
             * handleMessage() defines the operations to perform when
             * the Handler receives a new Message to process.
             */
            @Override
            public void handleMessage(Message inputMessage) {
                writeTerminal((String) inputMessage.obj);
            }
        };
    }

    /**
            * @param bitmap The source bitmap.
            * @param opacity a value between 0 (completely transparent) and 255 (completely opaque).
            * @return The opacity-adjusted bitmap.  If the source bitmap is mutable it will be
    * adjusted and returned, otherwise a new bitmap is created.
    */
    private Bitmap adjustOpacity(Bitmap bitmap, int opacity)
    {
        Bitmap mutableBitmap = bitmap.isMutable()
                ? bitmap
                : bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        int colour = (opacity & 0xFF) << 24;
        canvas.drawColor(colour, PorterDuff.Mode.DST_IN);
        return mutableBitmap;
    }

    public void startControllerActivity(){

        Singleton.getInstance().setString("Singleton");
        Singleton.getInstance().setMotorsBluetoothConnection(this.motors_blue_connection);
        Singleton.getInstance().setCameraMotorsBluetoothConnection(this.camera_motors_blue_connection);
        Intent intent = new Intent(this,ControllerUI.class);
        startActivity(intent);
    }

    /**
     * Add a new line of text to the textView_terminal
     * @param text
     * @return
     */
    private void writeTerminal(String text){

        Calendar calendar = Calendar.getInstance();
        textView_terminal.append("[" + String.valueOf(calendar.get(Calendar.HOUR)) + ":" +
                String.valueOf(calendar.get(Calendar.MINUTE)) + ":" +
                String.valueOf(calendar.get(Calendar.SECOND)) + "]" +
                text + "\n");
    }


    private class RingDialog {
        protected String state;

        public RingDialog(){
            state = "0";
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
                        // connect to the motors driving service
                        motors_blue_connection.DiscoverDevices();
                        motors_blue_connection.ConnectToRobot();

                        // connect to the camera motors driving service
                        camera_motors_blue_connection.DiscoverDevices();
                        camera_motors_blue_connection.ConnectToRobot();

                        // continue until the connection has been established
                        int counter = 0;
                        while (camera_motors_blue_connection.connectionState() == 3 && (counter < max_discovering_time)) {
                            Thread.sleep(100);
                            counter += 100;
                        }
                        if ((counter >= max_discovering_time)) {
                            state = "Maximum discoverying time exceeded ... ";
                        } else if (camera_motors_blue_connection.connectionState() == 1) {
                            state = "Connected with Johnny! :)";
                        }

                    } catch (Exception e) {
                        Log.d("RING DIALOG EXECEPTION", e.getMessage());
                    }

                    ringProgressDialog.dismiss();

                    sendTerminalHandlerMsg(state);


                }
            }).start();
        }
    }

    /**
     * Create a string message to send to the terminalHandler which prints it into the terminal
      * @param msg String message
     */
    protected void sendTerminalHandlerMsg(String msg){
        Message completeMsg = Message.obtain();
        completeMsg.obj = msg;
        completeMsg.setTarget(terminalHandler);
        completeMsg.sendToTarget();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

}
