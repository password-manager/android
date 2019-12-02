package com.example.passwordmanager;

import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerConnection {
    String username;
    private Socket socket;

    private static final int SERVERPORT = 5000;
    private static final String SERVER_IP = "192.168.2.184";

    static public ServerConnection single_instance;

    public ServerConnection(){

    }

    static public ServerConnection getInstance(){
        if (single_instance == null)
            single_instance = new ServerConnection();

        return single_instance;
    }

    public void register(String password, String salt){
        final Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Replace below IP with the IP of that device in which server socket open.
                    //If you change port then change the port number in the server side code also.
                    Socket s = new Socket("xxx.xxx.xxx.xxx", 9002);

                    OutputStream out = s.getOutputStream();

                    PrintWriter output = new PrintWriter(out);

                    output.println("msg");
                    output.flush();
                    BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    final String st = input.readLine();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (st.trim().length() != 0)
                                Log.i("TESTRESPONSE", st);
                        }
                    });

                    output.close();
                    out.close();
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void login(String password){

    }

    public void sendCode(String code){

    }

    public void sendTimestamp(String timestamp){

    }

    public void sendUpdate(String update){

    }

    public void sendLogs(JSONArray logs){

    }


}
