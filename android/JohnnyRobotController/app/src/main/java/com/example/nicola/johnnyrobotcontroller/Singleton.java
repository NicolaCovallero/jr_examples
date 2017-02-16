package com.example.nicola.johnnyrobotcontroller;

/**
 * Created by nicola on 31/01/17.
 * Singleton class to share data between activities
 */

public class Singleton {
    private static Singleton mInstance = null;

    private String mString;

    private String connection_style;
    private BluetoothConnection motors_blue_connection, camera_motors_blue_connection;// bluetooth connections


    private Singleton() {
        mString = "Hello";
    }

    public static Singleton getInstance() {
        if (mInstance == null) {
            mInstance = new Singleton();
        }
        return mInstance;
    }

    public String getString() {
        return this.mString;
    }

    public void setString(String value) {
        mString = value;
    }

    public void setConnectioStyle(String val){ connection_style = val; }

    public String getConnectionStyle() { return connection_style; }

    public void setMotorsBluetoothConnection(BluetoothConnection bc){
        motors_blue_connection = bc;
    }
    public BluetoothConnection getMotorsBluetoothConnection() {
        return this.motors_blue_connection;
    }
    public void setCameraMotorsBluetoothConnection(BluetoothConnection bc){
        camera_motors_blue_connection = bc;
    }
    public BluetoothConnection getCameraMotorsBluetoothConnection() {
        return this.camera_motors_blue_connection;
    }

}
