package com.example.passwordmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends Activity  implements AdapterView.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{
    Button newDirectoryButton, newPasswordButton, logoutButton, higherDirectory;
    String currentPath, masterPassword, username;
    CheckBox showPassword;
    JSONArray database;
    LocalDatabase localDatabase;
    ServerConnection serverConnection;




    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View v) {
        if (currentPath.equals("/")){
            return;
        }else{
            try {
                String[] pathArray = Arrays.copyOfRange(currentPath.split("/"), 0, currentPath.split("/").length-1);
                currentPath = String.join("/", pathArray) + "/";
                Log.i("TESTMAINAC", currentPath);
                JSONArray subdirectory = localDatabase.getDirectory(currentPath).getJSONArray("data");
                arrayOfItems.clear();
                JSONArray currentSubdir = new JSONArray();
                for (int i = 0; i < subdirectory.length(); i++){
                    if (!subdirectory.getJSONObject(i).optBoolean("deleted")){
                        currentSubdir.put(subdirectory.getJSONObject(i));
                    }
                }
                populateList(arrayOfItems, currentSubdir);
                ListView listView = (ListView) findViewById(R.id.main_passwordsList);
                ListableItemsAdapter adapter = new ListableItemsAdapter(getApplicationContext(), arrayOfItems);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        Intent intent = new Intent();
        String name = ((TextView) v.findViewById(R.id.name)).getText().toString();
        String type = ((TextView) v.findViewById(R.id.type)).getText().toString();
        String data = "";
        if (type.equals("password")){
            JSONArray dir = new JSONArray();
            try {
                dir= localDatabase.getDirectory(currentPath).getJSONArray("data");
                for (int i = 0; i < dir.length(); i++) {
                    JSONObject item = dir.getJSONObject(i);
                    if (item.optString("type").equals("password") && item.optString("name").equals(name)) {
                        data = item.optString("data");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            LayoutInflater inflater = (LayoutInflater)
                    getSystemService(LAYOUT_INFLATER_SERVICE);

            View popupView = inflater.inflate(R.layout.popup_window, null);
            TextView tv = (TextView) popupView.findViewById(R.id.textView6);
            tv.setText(name);
            final TextView tv2 = (TextView) popupView.findViewById(R.id.textView9);
            tv2.setText(data);
            CheckBox popupShowPassword = (CheckBox) popupView.findViewById(R.id.editPassword_showPassword);
            popupShowPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((CheckBox) v).isChecked()) {
                        (tv2).setTransformationMethod(null);
                    } else {
                        tv2.setTransformationMethod(new PasswordTransformationMethod());
                    }
                }
            });
            // create the popup window
            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true; // lets taps outside the popup also dismiss it
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            // show the popup window
            // which view you pass in doesn't matter, it is only used for the window tolken
            popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
        } else {
            //change directory
            try {
                currentPath = currentPath + name + "/";
                JSONArray subdirectory = localDatabase.getDirectory(currentPath).getJSONArray("data");
                arrayOfItems.clear();
                JSONArray currentSubdir = new JSONArray();
                for (int i = 0; i < subdirectory.length(); i++){
                    if (!subdirectory.getJSONObject(i).optBoolean("deleted")){
                        currentSubdir.put(subdirectory.getJSONObject(i));
                    }
                }
                populateList(arrayOfItems, currentSubdir);
                ListView listView = (ListView) findViewById(R.id.main_passwordsList);
                ListableItemsAdapter adapter = new ListableItemsAdapter(this, arrayOfItems);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //startActivity(intent);
    }

    public boolean onItemLongClick(AdapterView<?> arg0, View v,
                                   int position, long id) {
        Intent intent = new Intent();
        String name = ((TextView) v.findViewById(R.id.name)).getText().toString();
        String type = ((TextView) v.findViewById(R.id.type)).getText().toString();
        intent.putExtra("username", username);
        intent.putExtra("position", position);
        intent.putExtra("id", id);
        intent.putExtra("path", currentPath);
        intent.putExtra("name", name);
        //intent.putExtra("database", database.toString());
        intent.putExtra("operation_type", "edit");
        if (type.equals("password")){
            //edit password
            intent.setClass(this, EditPasswordActivity.class);
        } else {
            //edit directory
            intent.setClass(this, EditDirectoryActivity.class);
        }
        startActivity(intent);
        return true;
    }


    public void sendMessage(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    ArrayList<ListableItem> arrayOfItems = new ArrayList<ListableItem>();
    String file = username;
    private void populateList(List<ListableItem> arrayOfItems, JSONArray jsonMainNode) throws JSONException {
        for(int i = 0; i<jsonMainNode.length();i++){
            JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
            if (jsonChildNode.optString("type").equals("directory")){
                String type = "directory";
                String name = jsonChildNode.optString("name");
                JSONArray data = jsonChildNode.getJSONArray("data");
                arrayOfItems.add(new Directory(type, name, data));
            } else if (jsonChildNode.optString("type").equals("password")){
                String type = "password";
                String name = jsonChildNode.optString("name");
                String data = jsonChildNode.optString("data");
                arrayOfItems.add(new Password(type, name, data));
            }
        }
    }

    private void initList(){
        /*FileInputStream fin = null;
        try {
            String file = username;
            fin = openFileInput(file);
        } catch (FileNotFoundException e) {
            try {
                FileOutputStream fOut = openFileOutput(file, MODE_PRIVATE);
                fOut.write(emptyState.getBytes());
            } catch (FileNotFoundException e2) {
                e.printStackTrace();
            } catch (IOException e2) {
                e.printStackTrace();
            }
        }*/
        try{
            /*String temp = convertStreamToString(fin);
            database = new JSONArray(temp);
            fin.close();*/
            Log.i("localDatabase", "Tu problem");
            populateList(arrayOfItems, JSONops.filterDeleted(localDatabase.database.getJSONArray("data")));
            ListView listView = (ListView) findViewById(R.id.main_passwordsList);
            ListableItemsAdapter adapter = new ListableItemsAdapter(this, arrayOfItems);
            //SimpleAdapter simpleAdapter = new SimpleAdapter(this, employeeList, android.R.layout.simple_list_item_1, new String[] {"employees"}, new int[] {android.R.id.text1});
            listView.setAdapter(adapter);
            //ListView listview = (ListView) findViewById(R.id.listView1);
            listView.setOnItemClickListener(this);
            listView.setOnItemLongClickListener(this);
        }
        catch(JSONException e){
            Toast.makeText(getApplicationContext(), "Error"+e.toString(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onResume(){
        super.onResume();
        try {
            /*String file = username;
            FileInputStream fin = openFileInput(file);
            String temp = convertStreamToString(fin);
            database = new JSONArray(temp);
            fin.close();*/
            JSONArray subdirectory = localDatabase.getDirectory(currentPath).getJSONArray("data");
            subdirectory = JSONops.filterDeleted(subdirectory);
            arrayOfItems.clear();
            populateList(arrayOfItems, subdirectory);
            ListView listView = findViewById(R.id.main_passwordsList);
            ListableItemsAdapter adapter = new ListableItemsAdapter(this, arrayOfItems);
            //SimpleAdapter simpleAdapter = new SimpleAdapter(this, employeeList, android.R.layout.simple_list_item_1, new String[] {"employees"}, new int[] {android.R.id.text1});
            listView.setAdapter(adapter);
            //ListView listview = (ListView) findViewById(R.id.listView1);
            listView.setOnItemClickListener(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Bundle bundle = getIntent().getExtras();
        masterPassword = bundle.getString("master-password");
        username = bundle.getString("username");
        localDatabase = LocalDatabase.getInstance(username);
        serverConnection = ServerConnection.getInstance();
        ServerConnection.password = masterPassword;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        long currentTime = timestamp.getTime();
        try {
            serverConnection.sendTimestamp(currentTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        initList();
        FileOutputStream fOut = null;

        deleteFile("users");

        newDirectoryButton = (Button)findViewById(R.id.main_addDirectoryButton);
        newPasswordButton = (Button)findViewById(R.id.main_addPasswordButton);
        logoutButton = (Button)findViewById(R.id.main_logoutButton);
        higherDirectory = (Button)findViewById(R.id.main_exitDirectoryButton);
        currentPath = "/";



        newDirectoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("username", username);
                intent.putExtra("operation_type", "new");
                intent.putExtra("path", currentPath);
                //intent.putExtra("database", database.toString());
                intent.setClass(getApplicationContext(), EditDirectoryActivity.class);
                startActivity(intent);
            }
        });


        newPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("username", username);
                intent.putExtra("operation_type", "new");
                intent.putExtra("path", currentPath);
                //intent.putExtra("database", database.toString());
                intent.setClass(getApplicationContext(), EditPasswordActivity.class);
                startActivity(intent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        higherDirectory.setOnClickListener(this);
    }
}