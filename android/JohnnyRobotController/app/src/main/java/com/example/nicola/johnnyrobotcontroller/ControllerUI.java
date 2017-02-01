package com.example.nicola.johnnyrobotcontroller;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Calendar;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ControllerUI extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;

    private TextView textView_terminal;
    private ImageView motorsJoystick, cameraJoystick;

    private BluetoothConnection motors_blue_connection, camera_motors_blue_connection;// bluetooth connection

    // zero coordinates of joysticks
    private float camera_zx, camera_zy, motors_zx, motors_zy;

    // relative coordinates of joysticks
    private float camera_x, camera_y, motors_x, motors_y;
    private double motors_angle, motors_mag; // joystick variable
    private double camera_angle, camera_mag; // joystick variable
    private double camera_yaw_angle, camera_pitch_angle;

    protected boolean status_on;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        status_on = true; // ON
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_controller_ui);

        mContentView = findViewById(R.id.backgroundView);

        textView_terminal = (TextView) findViewById(R.id.textView_terminal);
        textView_terminal.setMovementMethod(new ScrollingMovementMethod());

        motorsJoystick = (ImageView) findViewById(R.id.motorsControllerView);
        cameraJoystick = (ImageView) findViewById(R.id.cameraControllerView);


        motorsJoystick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                motorsJoystickCallback();
            }
        });
        cameraJoystick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraJoystickCallback();
            }
        });
        cameraJoystick.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                float MAG_MAX = 100;

                if (event.getAction() == MotionEvent.ACTION_DOWN ||
                        event.getAction() == MotionEvent.ACTION_MOVE) {


                    writeTerminal("Touch coordinates : " +
                            String.valueOf(event.getX()) + "x  " + String.valueOf(event.getY()) + " Y");
                    //                float dx = v.getWidth()/2 - event.getX() ;
                    //                float dy = v.getHeight()/2 - event.getY() ;
                    float x = v.getWidth() / 2 - event.getX();
                    float y = v.getHeight() / 2 - event.getY();

                    // smooth movement
                    float th = 5;
                    if ((camera_x - x) < th) {
                        camera_x = camera_x + th;
                    } else if ((camera_x + x) < th) {
                        camera_x = camera_x - th;
                    }
                    if ((camera_y - y) < th) {
                        camera_y = camera_y + th;
                    } else if ((camera_y + y) < th) {
                        camera_y = camera_y - th;
                    }

                    // compute now the angle
                    camera_angle = Math.atan2(camera_x, camera_y);

                    // compute radius
                    camera_mag = Math.sqrt(Math.pow(camera_x, 2) + Math.pow(camera_y, 2));

                    if (camera_mag >= MAG_MAX) {
                        camera_x = (float) Math.cos(camera_angle) * MAG_MAX;
                        camera_y = (float) Math.sin(camera_angle) * MAG_MAX;
                    }

                    v.setX(camera_zx - camera_x);
                    v.setY(camera_zy - camera_y);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    // reset values
                    v.setX(camera_zx);
                    v.setY(camera_zy);
                    camera_x = 0;
                    camera_y = 0;
                    camera_angle = 0;
                    camera_mag = 0;
                }
                camera_yaw_angle = (camera_x / MAG_MAX) * 90;
                return true;
            }
        });
        motorsJoystick.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN ||
                        event.getAction() == MotionEvent.ACTION_MOVE) {
                    //writeTerminal("Touch coordinates : " +
                    //        String.valueOf(event.getX()) + "x  " + String.valueOf(event.getY()) + " Y");
                    //                float dx = v.getWidth()/2 - event.getX() ;
                    //                float dy = v.getHeight()/2 - event.getY() ;
                    float x = v.getWidth() / 2 - event.getX();
                    float y = v.getHeight() / 2 - event.getY();

                    double mag_xy = Math.sqrt(Math.pow(motors_x - x, 2) + Math.pow(motors_y - y, 2));
                    //Log.d("magnitude: ", String.valueOf(mag_xy));

                    // smooth movement
                    float th = 5;
                    if (mag_xy <= 100) {
                        if ((motors_x - x) < th) {
                            motors_x = motors_x + th;
                        } else if ((motors_x + x) < th) {
                            motors_x = motors_x - th;
                        } else {
                            motors_x = x;
                        }
                        if ((motors_y - y) < th) {
                            motors_y = motors_y + th;
                        } else if ((motors_y + y) < th) {
                            motors_y = motors_y - th;
                        } else {
                            motors_y = y;
                        }
                    }


                    // compute now the angle
                    motors_angle = Math.atan2(motors_y, motors_x);
                    writeTerminal("angle" + String.valueOf(motors_angle));

                    // compute radius
                    motors_mag = Math.sqrt(Math.pow(motors_x, 2) + Math.pow(motors_y, 2));

                    float MAG_MAX = 100;
                    if (motors_mag >= MAG_MAX) {
                        motors_x = (float) Math.cos(motors_angle) * MAG_MAX;
                        motors_y = (float) Math.sin(motors_angle) * MAG_MAX;
                    }

                    v.setX(motors_zx - motors_x);
                    v.setY(motors_zy - motors_y);

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    // reset values
                    v.setX(motors_zx);
                    v.setY(motors_zy);
                    motors_y = 0;
                    motors_x = 0;
                    motors_angle = 0;
                    motors_mag = 0;
                }

                return true;
            }
        });

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        // hide status and action bar
        hide();


        // to get the aboslute position of the image we have to wait it has been instantiated
        // reference: http://stackoverflow.com/questions/19497402/get-position-of-imageview-relative-to-screen-programmatically
        cameraJoystick.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                cameraJoystick.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int[] locations = new int[2];
                cameraJoystick.getLocationOnScreen(locations);
                camera_zx = locations[0];
                camera_zy = locations[1];
                writeTerminal(String.valueOf(camera_zx) + " /Y " + String.valueOf(camera_zy));

            }
        });
        motorsJoystick.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                motorsJoystick.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int[] locations = new int[2];
                motorsJoystick.getLocationOnScreen(locations);
                motors_zx = locations[0];
                motors_zy = locations[1];
                writeTerminal(String.valueOf(motors_zx) + " /Y " + String.valueOf(motors_zy));

            }
        });

        motors_blue_connection = Singleton.getInstance().getMotorsBluetoothConnection();
        camera_motors_blue_connection = Singleton.getInstance().getCameraMotorsBluetoothConnection();

        /*
        Send periodically a commands to the robot
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    SystemClock.sleep(5); // sleep 5 ms
                    if(status_on) {
                        if (motors_mag > 0) {

                            if (motors_y > 0) {
                                if (motors_angle > 3 * Math.PI / 4) {
                                    motors_blue_connection.sendString("right-0");
                                } else if (motors_angle < Math.PI / 4) {
                                    motors_blue_connection.sendString("left-0");
                                } else {
                                    motors_blue_connection.sendString("forward-0");
                                }
                            } else { // motors_y is negative
                                if (motors_angle < -3 * Math.PI / 4) {
                                    motors_blue_connection.sendString("right-0");
                                } else if (motors_angle > -Math.PI / 4) {
                                    motors_blue_connection.sendString("left-0");
                                } else {
                                    motors_blue_connection.sendString("backward-0");
                                }
                            }
                        }
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // simple method to synchronize the robot with the bluetooth communication
                    // is by slowing the thread down
                    SystemClock.sleep(10); // sleep 10 ms
                    if(status_on) {
                        camera_motors_blue_connection.sendStringSync("yaw/" +
                                String.valueOf(-camera_yaw_angle) + "/pitch/0");
                    }
                }
            }
        }).start();



        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        //delayedHide(100);
    }

    private void toggle() {
        hide();

    }

    /**
     * Hide action and status bar to make complete fullscreen mode
     */
    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        //mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

//    @SuppressLint("InlinedApi")
//    private void show() {
//        // Show the system bar
//        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
//        mVisible = true;
//
//        // Schedule a runnable to display UI elements after a delay
//        mHideHandler.removeCallbacks(mHidePart2Runnable);
//        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
//    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * Add a new line of text to the textView_terminal
     *
     * @param text
     * @return
     */
    private Void writeTerminal(String text) {
        Calendar calendar = Calendar.getInstance();
        textView_terminal.append("[" + String.valueOf(calendar.get(Calendar.HOUR)) + ":" +
                String.valueOf(calendar.get(Calendar.MINUTE)) + ":" +
                String.valueOf(calendar.get(Calendar.SECOND)) + "]" +
                text + "\n");
        // gravity bottom 8set in xml) is not working :(
        // so automatic programmatically scroll is done
        textView_terminal.scrollBy(0, Integer.MAX_VALUE);
        return null;
    }

    private void cameraJoystickCallback() {
        writeTerminal("clicked camera joystick");
    }

    private void motorsJoystickCallback() {
        writeTerminal("clicked motors joystick");
    }


    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ControllerUI Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.nicola.johnnyrobotcontroller/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        status_on = false; // OFF
    }


    @Override
    protected void onPause(){
        super.onPause();
        status_on = false; // OFF
    }

    @Override
    protected void onResume(){
        super.onResume();
        status_on = true; // ON;

        motors_blue_connection = Singleton.getInstance().getMotorsBluetoothConnection();
        camera_motors_blue_connection = Singleton.getInstance().getCameraMotorsBluetoothConnection();
    }
}
