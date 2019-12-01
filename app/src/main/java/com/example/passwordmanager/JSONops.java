package com.example.passwordmanager;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class JSONops {
    static public JSONObject getDirectory(String path, JSONObject database) {
        try{
            if (path.equals("/")) return database;
            String[] pathArray = Arrays.copyOfRange(path.split("/"), 1, path.split("/").length);
            JSONArray directory = database.getJSONArray("data");
            for (String directoryName : pathArray){
                for (int i = 0; i < directory.length(); i++){
                    JSONObject item = directory.getJSONObject(i);
                    if (item.optString("type").equals("directory") && item.optString("name").equals(directoryName)){
                        return item;
                    }
                }
            }
        }catch (Exception e){
            Log.i("ERROR", "JSONops.getPassword");
        }
        return null;
    }

    static public JSONObject getPassword(String path, JSONObject database){
        if (path.equals("/")) return null;
        try {
            String[] pathArray = Arrays.copyOfRange(path.split("/"), 1, path.split("/").length);
            JSONArray directory = database.getJSONArray("data");
            for (String directoryName : pathArray) {
                for (int i = 0; i < directory.length(); i++) {
                    JSONObject item = directory.getJSONObject(i);
                    if (item.optString("type").equals("password") && item.optString("name").equals(directoryName)) {
                        return item;
                    }
                }
            }
        }catch (Exception e){
            Log.i("ERROR", "JSONops.getPassword");
        }
        return null;
    }


}
