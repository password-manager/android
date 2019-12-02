package com.example.passwordmanager;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class JSONops {
    static public JSONObject getDirectory(String path, JSONObject database) {
        try{
            Log.i("TEST", "path" + path);
            if (path.equals("/")) return database;
            String[] pathArray = Arrays.copyOfRange(path.split("/"), 1, path.split("/").length);
            Log.i("TEST", "patharray" + pathArray.length + "\n" + Arrays.toString(pathArray));
            JSONArray directory;// = database.getJSONArray("data");
            JSONObject item = database;
            for (String directoryName : pathArray){
                directory = database.getJSONArray("data");
                for (int i = 0; i < directory.length(); i++){
                    item = directory.getJSONObject(i);
                    if (!item.optBoolean("deleted") &&
                            item.optString("type").equals("directory") &&
                            item.optString("name").equals(directoryName)){
                        database = item;
                    }
                }
            }
            return database;
        }catch (Exception e){
            Log.i("ERROR", "JSONops.getPassword");
        }
        return null;
    }

    static public JSONObject getPassword(String path, JSONObject database){
        if (path.equals("/")) return null;
        try {
            String[] a = splitFullPath(path);
            Log.i("TESTGETPASS", Arrays.toString(a));
            JSONArray directory = getDirectory(a[0], database).getJSONArray("data");
            for (int i = 0; i < directory.length(); i++) {
                JSONObject item = directory.getJSONObject(i);
                if (!item.optBoolean("deleted") &&
                        item.optString("type").equals("password") &&
                        item.optString("name").equals(a[1])) {
                    return item;
                }
            }
        }catch (Exception e){
            Log.i("ERROR", "JSONops.getPassword");
        }
        return null;
    }

    static public String[] splitFullPath(String path){
        //path = path.substring(0, path.length() - 1);
        int i = path.lastIndexOf("/");
        String[] a =  {path.substring(0, i+1), path.substring(i+1)};
        return a;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    static public void deleteDirectory(String path, JSONObject database){
        try {
            String[] a = JSONops.splitFullPath(path);
            JSONArray directory = getDirectory(a[0], database).getJSONArray("data");
            for (int i = 0; i < directory.length(); i++) {
                JSONObject item = directory.getJSONObject(i);
                if (!item.optBoolean("deleted") &&
                        item.optString("type").equals("directory") &&
                        item.optString("name").equals(a[1])) {
                    item.put("deleted", true);
                    break;
                }
            }
        } catch (Exception e){
            Log.i("ERROR", "DELETE DIRECTORY");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    static public void deletePassword(String path, JSONObject database){
        try {
            Log.i("TEST", "deletingstart");
            String[] a = JSONops.splitFullPath(path);
            JSONArray directory = getDirectory(a[0], database).getJSONArray("data");
            for (int i = 0; i < directory.length(); i++) {
                JSONObject item = directory.getJSONObject(i);
                Log.i("TEST",  a[1] + "\n"+item.toString());
                if (!item.optBoolean("deleted") &&
                        item.optString("type").equals("password") &&
                        item.optString("name").equals(a[1])) {
                    item.put("deleted", true);
                    Log.i("TEST", "FLAGGED");
                    break;
                }
            }
        } catch (Exception e){
            Log.i("ERROR", "DELETE DIRECTORY");
        }
    }

    static public boolean isUnique(String currentPath, String name, String type, JSONObject database) {
        try {
            JSONArray directory = getDirectory(currentPath, database).getJSONArray("data");
            for (int i = 0; i < directory.length(); i++) {
                JSONObject item = directory.getJSONObject(i);
                if (item.optString("type").equals(type) && item.optString("name").equals(name) && !item.optBoolean("deleted")) {
                    return false;
                }
            }
            return true;
        } catch (Exception e){
            Log.i("ERROR", "isUnique");
        }
        return false;
    }

    static public void safeAdd(String path, JSONObject node, JSONObject database){
        try {
            String[] a = splitFullPath(path);
            boolean unique = isUnique(path, node.getString("name"), node.getString("type"), database);
            if (unique) {
                JSONArray dir = getDirectory(path, database).getJSONArray("data");
                dir.put(node);
            }
        } catch (Exception e){
            Log.i("ERROR", "safeAdd");
        }
    }


    static public void safeModify(String path, JSONObject node, JSONObject database){
        try {
            String[] a = splitFullPath(path);
            boolean unique = true;
            if (!node.getString("name").equals(a[1])) {
                unique = isUnique(a[0], node.getString("name"), node.getString("type"), database);
            }
            if (unique) {
                JSONArray directory = getDirectory(a[0], database).getJSONArray("data");
                for (int i = 0; i < directory.length(); i++) {
                    JSONObject item = directory.getJSONObject(i);
                    if (!item.optBoolean("deleted") &&
                            item.optString("type").equals(node.getString("type")) &&
                            item.optString("name").equals(a[1])) {
                        directory.put(i, node);
                        return;
                    }
                }
            }
        } catch (Exception e){
            Log.i("ERROR", "safeAdd");
        }
    }

    static public JSONArray filterDeleted(JSONArray array){
        try {
            JSONArray a = new JSONArray();
            JSONObject item;
            for (int i = 0; i < array.length(); i++) {
                item = array.getJSONObject(i);
                if (!item.optBoolean("deleted")) {
                    a.put(item);
                }
            }
            return a;
        } catch (Exception e){
            Log.i("ERROR", "filterDeleted");
        }
        return null;
    }
}
