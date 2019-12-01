package com.example.passwordmanager;

import org.json.JSONArray;

public class ServerConnection {
    String username;

    static public ServerConnection single_instance;

    public ServerConnection(){

    }

    static public ServerConnection getInstance(){
        if (single_instance == null)
            single_instance = new ServerConnection();

        return single_instance;
    }

    public void register(String password, String salt){

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
