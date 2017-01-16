package com.example.nicola.johnnyrobotcontroller;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import java.util.Calendar;



public class MainActivity extends AppCompatActivity {

    private Bitmap js_motors, js_camera;
    private Button button_connect, button_start;
    private CheckBox cb_bluetooth, cb_wifi;
    private ImageView imageView;
    private TextView textView_terminal;
    private boolean connection_wifi = true; // false when the connection is via bluetooth
    private Handler textHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_connect = (Button) findViewById(R.id.button_connect);
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
                    connection_wifi = false;
                    cb_bluetooth.setChecked(true);
                    cb_wifi.setChecked(false);
                    writeTerminal("Chosen bluetooth communication, good choice bro! ;)");
                }
                else {
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
                    connection_wifi = true;
                    cb_bluetooth.setChecked(false);
                    cb_wifi.setChecked(true);
                    writeTerminal("Chosen wifi connection. We are sorry but the app still cannot"+
                            " communicate with johnny via wifi :(");
                }
                else {
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
        Intent intent = new Intent(this, ControllerUI.class);
        //Intent intent = new Intent(this, fullactivity_example.class);
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    /**
     * Add a new line of text to the textView_terminal
     * @param text
     * @return
     */
    private Void writeTerminal(String text){
//        String text_view_text=textView_terminal.getText().toString();
//        StringBuffer sb=new StringBuffer(text_view_text);
//        Calendar calendar = Calendar.getInstance();
//        sb.append("[" + String.valueOf(calendar.get(Calendar.HOUR)) + ":" +
//                String.valueOf(calendar.get(Calendar.MINUTE)) + ":" +
//                String.valueOf(calendar.get(Calendar.SECOND)) + "]" +
//                text + "\n");
//        textView_terminal.setText(sb.toString());
        Calendar calendar = Calendar.getInstance();
        textView_terminal.append("[" + String.valueOf(calendar.get(Calendar.HOUR)) + ":" +
                String.valueOf(calendar.get(Calendar.MINUTE)) + ":" +
                String.valueOf(calendar.get(Calendar.SECOND)) + "]" +
                text + "\n");
        return null;
    }


}
