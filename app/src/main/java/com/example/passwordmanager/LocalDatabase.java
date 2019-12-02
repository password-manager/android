package com.example.passwordmanager;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

//import static com.example.passwordmanager.MainActivity.convertStreamToString;

public class LocalDatabase {
    volatile public JSONObject lastRemote;
    volatile public JSONObject database;
    static volatile public String username;
    static volatile public Context context = null;
    private static LocalDatabase single_instance = null;
    private Cryptography.EnCryptor encryptor;
    private Cryptography.DeCryptor decryptor;
    private static String emptyState = "[\n" +
            "  {\n" +
            "    \"type\": \"directory\",\n" +
            "    \"name\": \"root\",\n" +
            "    \"data\": []\n" +
            "  },\n" +
            "  {\n" +
            "    \"type\": \"directory\",\n" +
            "    \"name\": \"root\",\n" +
            "    \"data\": []\n" +
            "  }\n" +
            "]";

    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while (true) {
                if ((line = reader.readLine()) == null) break;
                sb.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public LocalDatabase(String user){
        FileInputStream fin = null;
        Log.i("TESTLocalDatabase", "ConstructorStart");
        try {
            username = user;
            fin = context.openFileInput(username);
        } catch (FileNotFoundException e) {
            try {
                //FIRST TIME LOGGING ON THIS DEVICE
                encryptor = new Cryptography.EnCryptor();
                decryptor = new Cryptography.DeCryptor();
                FileOutputStream fOut = context.openFileOutput(username, Context.MODE_PRIVATE);
                byte[] encryptedText = encryptor.encryptText(username, emptyState);
                Log.i("TESTEncrypted", new String(encryptedText));
                fOut.write(Base64.encodeToString(encryptedText, Base64.DEFAULT).getBytes());
                fOut.close();
                Log.i("TESTLocalDatabase", "Writeemptystate");
            } catch (FileNotFoundException e2) {
                e.printStackTrace();
            } catch (IOException e2) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e1) {
                e1.printStackTrace();
            } catch (NoSuchProviderException e1) {
                e1.printStackTrace();
            } catch (InvalidAlgorithmParameterException e1) {
                e1.printStackTrace();
            } catch (NoSuchPaddingException e1) {
                e1.printStackTrace();
            } catch (InvalidKeyException e1) {
                e1.printStackTrace();
            } catch (Exception e4){

            }
        }
        try{
            Log.i("TESTConstructor0", "");
            fin = context.openFileInput(username);
            String temp = convertStreamToString(fin);
            Log.i("TESTConstructor1", temp);
            temp = decryptor.decryptData(username, Base64.decode(temp, Base64.DEFAULT), encryptor.getIv());
            Log.i("TESTConstructor", temp);
            JSONArray fullDatabase = new JSONArray(temp);
            lastRemote = fullDatabase.getJSONObject(0);
            database = fullDatabase.getJSONObject(1);
            fin.close();
        }catch (Exception e){
            Log.i("TESTLocalDatabase", "Constructor");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    static public LocalDatabase getInstance(String name){
        if (single_instance == null && !name.equals(username))
            single_instance = new LocalDatabase(name);

        return single_instance;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void save(){
        try{
            FileOutputStream fOut = context.openFileOutput(username, Context.MODE_PRIVATE);
            JSONArray fullDatabase = new JSONArray();
            fullDatabase.put(lastRemote);
            fullDatabase.put(database);
            byte[] encryptedText = encryptor.encryptText(username, fullDatabase.toString());

            fOut.write(Base64.encodeToString(encryptedText, Base64.DEFAULT).getBytes());
            fOut.close();
        }catch (Exception e){
            Log.i("TESTLocalDatabase", "Constructor");
        }
    }

    public String toString(){
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void synchronize(JSONArray remoteLogs){
        try {
            //EnhancedState local_state = new EnhancedState(old_state, local_logs)
            JSONObject localState = database;
            //EnhancedState server_state = new EnhancedState(old_state, server_logs)
            JSONObject serverState = enhanceState(lastRemote, remoteLogs);
            //EnhancedState enhanced_new_state = merge_states(old_state, local_state, server_state)
            JSONObject enhancedNewState = mergeStates(lastRemote, localState, serverState);
            //update_server(server_state, enhanced_new_state)
            updateServer(serverState, enhancedNewState);
            //State new_state = clean_state(enhanced_new_state)
            JSONObject newState = diminishState(enhancedNewState);
            //save_as_current_state(new_state)
            this.database = new JSONObject(newState.toString());
            //save_as_old_state(new_state)
            this.lastRemote = new JSONObject(newState.toString());
            //clear_current_logs()*/
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
                    JSONObject newPassword = new JSONObject(log.getJSONObject("data").toString());
                    JSONops.safeModify(log.optString("path"), newPassword, enhancedState);//TODO timestamps
                }
                if (log.get("type").equals("modify_directory")){
                    JSONObject directory = JSONops.getPassword(log.optString("path"), enhancedState);
                    directory.put("name", log.optJSONObject("name"));//TODO
                }
                if (log.get("type").equals("delete_password"))
                    JSONops.deletePassword(log.optString("path"), enhancedState);//TODO
                if (log.get("type").equals("delete_directory"))
                    JSONops.deleteDirectory(log.optString("path"), enhancedState);//TODO
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

    public JSONObject getDirectory(String path) {
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void deleteDirectory(String path){
        JSONops.deleteDirectory(path, database);
        save();
        //TODO zapisz timestampa loga wyślij
    }
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void deletePassword(String path){
        JSONops.deletePassword(path, database);
        save();
        //TODO loga wyślij
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void safeAdd(String path, JSONObject node){
        JSONops.safeAdd(path, node, database);
        save();
        //TODO
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void safeModify(String path, JSONObject node){
        JSONops.safeModify(path, node, database);
        save();
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

    public boolean isUnique(String currentPath, String name, String type) {
        return JSONops.isUnique(currentPath, name, type, database);
    }
}
