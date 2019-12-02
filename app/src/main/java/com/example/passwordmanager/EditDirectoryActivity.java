package com.example.passwordmanager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class EditDirectoryActivity extends Activity {
    Button b1, b2, b3;
    EditText ed1, ed2;
    String name = "", currentPath, fullPath, masterPassword, operation_type, username;
    JSONArray database;
    LocalDatabase localDatabase = null;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void saveChanges(String path) throws JSONException {

        /*JSONArray directory = database;
        if (!path.equals("/")) {
            String[] pathArray = Arrays.copyOfRange(path.split("/"), 1, path.split("/").length);
            for (String directoryName : pathArray) {
                for (int i = 0; i < directory.length(); i++) {
                    JSONObject item = directory.getJSONObject(i);
                    if (item.optString("type").equals("directory") && item.optString("name").equals(directoryName)) {
                        directory = item.getJSONArray("data");
                        break;
                    }
                }
            }
        }*/
        if (operation_type.equals("edit")) {
            JSONObject newDir = new JSONObject(localDatabase.getDirectory(fullPath+"/").toString());
            newDir.put("name", ed1.getText().toString());
            localDatabase.safeModify(fullPath, newDir);
            /*for (int i = 0; i < directory.length(); i++) {
                JSONObject item = directory.getJSONObject(i);
                if (item.optString("type").equals("directory") && item.optString("name").equals(name)) {
                    item.put("name", ed1.getText().toString());
                    break;
                }
            }*/
        } else if (operation_type.equals("delete")) {
            localDatabase.deleteDirectory(fullPath);
            /*for (int i = 0; i < directory.length(); i++) {
                JSONObject item = directory.getJSONObject(i);
                if (item.optString("type").equals("directory") && item.optString("name").equals(name)) {
                    directory.remove(i);
                    break;
                }
            }*/
        } else {
            Log.i("TEST","else");
            JSONObject newItem = new JSONObject();
            newItem.put("type", "directory");
            newItem.put("name", ed1.getText().toString());
            newItem.put("data", new JSONArray());
            localDatabase.safeAdd(currentPath, newItem);
        }
        //saveDatabase();
        finish();
    }

    /*private void saveDatabase() {
        String file = username;
        try {
            FileOutputStream fOut = openFileOutput(file, MODE_PRIVATE);
            fOut.write(database.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory);


        b1 = (Button) findViewById(R.id.editDirectory_saveButton);
        b2 = (Button) findViewById(R.id.editDirectory_cancelButton);
        b3 = (Button) findViewById(R.id.editDirectory_deleteButton);
        ed1 = (EditText) findViewById(R.id.editDirectory_editName);
        Log.i("TEST","BUTTONSEND");
        Intent intent = getIntent();
        //String databaseString = intent.getStringExtra("database");
        username = intent.getStringExtra("username");
        currentPath = intent.getStringExtra("path");
        operation_type = intent.getStringExtra("operation_type");
        localDatabase = LocalDatabase.getInstance(username);
        if (!operation_type.equals("edit")) {

            ((TextView) findViewById(R.id.editDirectory_textView)).setText("CREATE DIRECTORY");
            b3.setVisibility(View.GONE);
        } else {
            name = intent.getStringExtra("name");
            fullPath = currentPath + name;
        }
        Log.i("TEST","INTENT");
        /*try {
            database = new org.json.JSONArray(databaseString);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        if (operation_type.equals("edit")) {
            ed1.setText(name);
            /*try {
                JSONArray directory = getDirectory(currentPath);
                for (int i = 0; i < directory.length(); i++) {
                    JSONObject item = directory.getJSONObject(i);
                    if (item.optString("type").equals("directory") && item.optString("name").equals(name)) {
                        //password = item.getJSONArray("data");
                        ed1.setText(item.optString("name"));
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }*/
        }
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.i("TEST","Button pressed, operation_type" + operation_type);
                    saveChanges(currentPath);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    operation_type = "delete";
                    saveChanges(currentPath);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}