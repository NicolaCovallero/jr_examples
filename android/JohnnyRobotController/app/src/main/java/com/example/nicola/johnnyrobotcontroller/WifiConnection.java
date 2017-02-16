package com.example.nicola.johnnyrobotcontroller;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by nicola on 16/02/17.
 */
public class WifiConnection {

    protected String RASP_IP = "192.168.0.12"; // raspberry's IP default address
    protected DatagramSocket socket;
    protected InetAddress local;
    protected int server_port;


    public WifiConnection(){

        try {
            socket = new DatagramSocket();
            local = InetAddress.getByName(RASP_IP);
        }
        catch (Exception e){
            Log.d("EXCEPTION UDP SOCKET", e.getMessage());
        }

    }

    public void connect(int port){
        try{
            socket.connect(local, port);
            server_port = port;
        }
        catch (Exception e){

        }

    }

    // TODO: implement the followings functions as runnable.
    /**
     * Send string data. For the moment it should be used inside a runnable.
     * @<code>
     *     new Thread(new Runnable() {
     *       @Override
     *       public void run() {
     *          connection_socket.sendStringData("connected");
     *       }
     *       }).start();;
     * </code>
     * @param data
     */
    public boolean sendStringData(String data){

        try{
            if (socket.isConnected()){
                DatagramPacket p = new DatagramPacket(data.getBytes(), data.length(), local,
                        server_port);
                socket.send(p);
            }
            else{
                Log.d("UDP SOCKET","Socket not connected");
            }
            return true;
        }
        catch (Exception e){
            Log.d("EXCEP: SENDING DATA", e.getMessage());
            return false;
        }

    }

    /**
     * @<code>
     *     new Thread(new Runnable() {
     *       @Override
     *       public void run() {
     *           String msg_rcv = connection_socket.receiveStringData();
     *       }
     *       }).start();;
     * </code>
     * @return
     */
    public String receiveStringData(){
        try{
            if (socket.isConnected()){
                    byte[] buffer = new byte[1024];
                DatagramPacket p = new DatagramPacket(buffer,buffer.length, local, server_port);
                socket.receive(p);
                return new String(p.getData(), "UTF-8");
            }
            else{
                Log.d("UDP SOCKET","Socket not connected");
                return "not connected";
            }

        }
        catch (Exception e){
            Log.d("EXCEP: RECEIVING DATA", e.getMessage());
            return "false";
        }
    }


}
