package com.example.passwordmanager;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static com.example.passwordmanager.MainActivity.convertStreamToString;

public class LocalDatabase {
    public JSONObject lastRemote;
    public JSONObject database;
    public String username;
    static public Context context = null;
    private static LocalDatabase single_instance = null;

    public LocalDatabase(String username){
        try {
            this.username = username;
            String file = username;
            FileInputStream fin = context.openFileInput(file);
            String temp = convertStreamToString(fin);
            JSONArray fullDatabase = new JSONArray(temp);
            lastRemote = fullDatabase.getJSONObject(0);
            database = fullDatabase.getJSONObject(1);
            fin.close();
        }catch (Exception e){
            Log.i("LocalDatabase", "Constructor");
        }
    }

    public LocalDatabase getInstance(String username){
        if (single_instance == null)
            single_instance = new LocalDatabase(username);

        return single_instance;
    }

    public String toString(){
        return null;
    }

    public void synchronize(JSONArray remoteLogs){
        //EnhancedState local_state = new EnhancedState(old_state, local_logs)
        JSONObject localState = database;
        //EnhancedState server_state = new EnhancedState(old_state, server_logs)
        JSONObject serverState = enhanceState(lastRemote, remoteLogs);
        //EnhancedState enhanced_new_state = merge_states(old_state, local_state, server_state)
        JSONObject enhancedNewState = mergeStates(lastRemote, localState, serverState);
        //update_server(server_state, enhanced_new_state)
        //TODO
        //State new_state = clean_state(enhanced_new_state)
        JSONObject newState = diminishState(enhancedNewState);
        //save_as_current_state(new_state)
        this.database = newState; //TODO copy?
        //save_as_old_state(new_state)
        this.lastRemote = newState; //TODO copy?
        //clear_current_logs()*/
    }

    public JSONObject enhanceState(JSONObject oldState, JSONArray logs){
        try{
            JSONObject enhancedState = new JSONObject(oldState.toString());
            for (int i = 0; i < logs.length(); i++) {
                JSONObject log = logs.optJSONObject(i);
                if (log.getString("type").equals("create_password"))
                    JSONops.getDirectory(log.optString("path"), enhancedState).getJSONArray("data").put(log.optJSONObject("data"));
                if (log.get("type").equals("create_directory"))
                    JSONops.getDirectory(log.optString("path"), enhancedState).getJSONArray("data").put(log.optJSONObject("data"));
                if (log.get("type").equals("modify_password")) {
                    JSONObject password = JSONops.getPassword(log.optString("path"), enhancedState);
                    password = log.optJSONObject("data");//TODO
                }
                if (log.get("type").equals("modify_directory")){
                    JSONObject directory = JSONops.getPassword(log.optString("path"), enhancedState);
                    directory.put("name", log.optJSONObject("name"));//TODO
                }
                if (log.get("type").equals("delete_password"))
                    deletePassword(log.optString("path"), enhancedState);//TODO
                if (log.get("type").equals("delete_directory"))
                    deleteDirectory(log.optString("path"), enhancedState);//TODO
                //foreach directory in log.path
                String[] dirs = log.optString("path").split("/");
                String dir = "/";
                int j = 1;
                while (!dir.equals(log.getString("path"))) {
                    getDirectory(dir).put("last_modified", log.getDouble("timestamp"));
                    dir = dir+"/"+dirs[j];
                    j++;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject diminishState(JSONObject enhancedState){
        try {
            JSONObject diminishedState = new JSONObject();
            if (!enhancedState.optBoolean("deleted") && enhancedState.getString("type").equals("directory")){
                JSONArray dir = enhancedState.getJSONArray("data");
                JSONArray newDir = new JSONArray();
                for (int i = 0; i < enhancedState.length(); i++) {
                    JSONObject node = dir.getJSONObject(i);
                    newDir.put(diminishState(node));
                }
                return new JSONObject()
                        .put("type", "directory")
                        .put("name", enhancedState.getString("name"))
                        .put("data", newDir);
            } else if (!enhancedState.optBoolean("deleted") && enhancedState.getString("type").equals("password")) {
                JSONObject newNode = new JSONObject()
                        .put("type", "password")
                        .put("name", enhancedState.getString("name"))
                        .put("data", enhancedState.getString("data"));
                return newNode;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getDirectory(String path) throws JSONException {
        return JSONops.getDirectory(path, database);
        /*if (path.equals("/")) return database;
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
        return null;*/
    }

    public JSONObject getPassword(String path){
        return JSONops.getPassword(path, database);
    }

    public void deleteDirectory(String path){
        //TODO
    }
    public void deletePassword(String path){
        //TODO
    }

    public void deleteDirectory(String path, JSONObject database){
        //TODO
    }
    public void deletePassword(String path, JSONObject database){
        //TODO
    }

    public void saveAdd(String path, JSONObject node){
        //TODO
    }

    public void saveModify(String path, JSONObject node){
        //TODO
    }
    //arguments have to be directories
    public JSONArray createUpdateLogs(JSONObject serverState, JSONObject enhancedNewState, String path){
        JSONArray updateLogs = new JSONArray();
        //for i from 1 to enhanced_new_state.length
        try {
            JSONArray serverDir = serverState.getJSONArray("data");
            JSONArray enhancedNewDir = enhancedNewState.getJSONArray("data");
            for (int i = 0; i < enhancedNewDir.length(); i++) {
                JSONObject log = new JSONObject();
                JSONObject node = enhancedNewDir.getJSONObject(i);
                if (i >= serverState.length() && !node.getBoolean("deleted")) {
                    log.put("type", "create_" + node.getString("type"));
                    log.put("path", path);
                    JSONObject data = new JSONObject(node.toString());
                    data.put("data", new JSONArray());
                    log.put("data", data);
                    updateLogs.put(log);
                } else if (i < serverState.length() && node.getBoolean("deleted")) {
                    log.put("type", "delete_"+ node.getString("type"));
                    log.put("path", path);
                    updateLogs.put(log);
                } else if (i < serverState.length() &&
                        (!node.getString("name").equals(serverDir.getJSONObject(i).getString("name")) ||
                                !node.getString("data").equals(serverDir.getJSONObject(i).getString("data")))) {
                    log.put("type", "modify_"+node.getString("type"));
                    log.put("path", path);
                    log.put("data", node);
                    updateLogs.put(log);
                }
                if (i < serverState.length() && serverState.getString("type").equals("directory")) {
                    String name = enhancedNewState.getString("name");
                    JSONArray logs = createUpdateLogs(serverDir.getJSONObject(i), enhancedNewDir.getJSONObject(i), path+"/"+name);
                    for (int j = 0; j < logs.length(); j++)
                        updateLogs.put(logs.getJSONObject(j));
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject mergeStates(JSONObject oldState, JSONObject localState, JSONObject serverState){
        try {
            JSONObject newState = new JSONObject()
                    .put("type", localState.getString("type"))
                    .put("name", localState.getString("name"));
            if (newState.getString("type").equals("password")) {
                if (localState.optDouble("last_modified", 0) > serverState.optDouble("last_modified", 0)) {
                    return localState;
                } else {
                    return serverState;
                }
            }
            //for i from 1 to old_state.length	//old_state.length - number of nodes in the directory old_state
            JSONArray oldDir = oldState.getJSONArray("data");
            JSONArray localDir = oldState.getJSONArray("data");
            JSONArray serverDir = oldState.getJSONArray("data");
            for (int i = 0; i < oldDir.length(); i++) {
                JSONObject newNode = new JSONObject();
                JSONObject oldNode = oldDir.getJSONObject(i);
                JSONObject localNode = localDir.getJSONObject(i);
                JSONObject serverNode = serverDir.getJSONObject(i);
                newNode = mergeStates(oldNode, localNode, serverNode);
                newState.getJSONArray("data").put(newNode);
            }
            for (int i = oldDir.length(); i < localDir.length(); i++) {
                if (localDir.getJSONObject(i).getBoolean("deleted"))
                    continue;
                else
                    mergeNode(newState, localDir.getJSONObject(i));
            }
            for (int i = oldDir.length(); i < serverDir.length(); i++) {
                if (serverDir.getJSONObject(i).getBoolean("deleted"))
                    continue;
                else
                    mergeNode(newState, serverDir.getJSONObject(i));
            }
            return newState;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public JSONObject mergeNode(JSONObject newState, JSONObject newNode){
        try {
            boolean added = false;
            //if new_node is directory
            JSONArray newDir = newState.getJSONArray("data");
            if (newNode.getString("type").equals("directory")) {
                for (int i = 0; i < newDir.length(); i++) {
                    JSONObject node = newDir.getJSONObject(i);
                    if (newNode.getString("name").equals(node.getString("name")) &&
                            node.getString("type").equals("directory") &&
                            !node.getBoolean("deleted")) {
                        JSONObject empty = new JSONObject().put("type", "directory")
                                .put("data", new JSONArray());
                        newDir.put(mergeStates(empty, node, newNode));
                        added = true;
                    }
                }
                if (!added)
                    newDir.put(newNode);
            } else if (newNode.getString("type").equals("password")) {
                for (int i = 0; i < newDir.length(); i++) {
                    JSONObject node = newDir.getJSONObject(i);
                    if (newNode.getString("name").equals(node.getString("name")) &&
                            node.getString("type").equals("password") &&
                            !node.getBoolean("deleted")) {
                        newDir.put(mergeStates(null, node, newNode));
                        added = true;
                    }
                }
                if (!added)
                    newDir.put(newNode);
            }
            return newState;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void updateServer(JSONObject serverState, JSONObject enhancedNewState){
        JSONArray update_logs = createUpdateLogs(serverState, enhancedNewState, "/");
        ServerConnection sc = ServerConnection.getInstance();
        sc.sendLogs(update_logs);
    }
}
