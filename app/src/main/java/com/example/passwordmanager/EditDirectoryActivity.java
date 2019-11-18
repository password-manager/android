package com.example.passwordmanager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class EditDirectoryActivity extends Activity {
    Button b1, b2, b3;
    EditText ed1, ed2;
    String name, currentPath, masterPassword, operation_type, username;
    JSONArray database;
    TextView tx1;
    int counter = 3;

    public JSONArray getDirectory(String path) throws JSONException {
        if (path.equals("/")) return database;
        String[] pathArray = Arrays.copyOfRange(path.split("/"), 1, path.split("/").length);
        JSONArray directory = database;
        for (String directoryName : pathArray) {
            for (int i = 0; i < directory.length(); i++) {
                JSONObject item = directory.getJSONObject(i);
                if (item.optString("type").equals("directory") && item.optString("name").equals(directoryName)) {
                    directory = item.getJSONArray("data");
                    break;
                }
            }
        }
        return directory;
    }

    public void saveChanges(String path) throws JSONException {
        JSONArray directory = database;
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
        }
        if (operation_type.equals("edit")) {
            for (int i = 0; i < directory.length(); i++) {
                JSONObject item = directory.getJSONObject(i);
                if (item.optString("type").equals("directory") && item.optString("name").equals(name)) {
                    item.put("name", ed1.getText().toString());
                    break;
                }
            }
        } else {
            JSONObject newItem = new JSONObject();
            newItem.put("type", "directory");
            newItem.put("name", ed1.getText().toString());
            newItem.put("data", new JSONArray());
            directory.put(newItem);
        }
        saveDatabase();
        finish();
    }

    private void saveDatabase() {
        String file = username;
        try {
            FileOutputStream fOut = openFileOutput(file, MODE_PRIVATE);
            fOut.write(database.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory);

        b1 = (Button) findViewById(R.id.editDirectory_saveButton);
        b2 = (Button) findViewById(R.id.editDirectory_cancelButton);
        ed1 = (EditText) findViewById(R.id.editDirectory_editName);
        Intent intent = getIntent();
        String databaseString = intent.getStringExtra("database");
        username = intent.getStringExtra("username");
        currentPath = intent.getStringExtra("path");
        operation_type = intent.getStringExtra("operation_type");
        name = intent.getStringExtra("name");
        try {
            database = new org.json.JSONArray(databaseString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (operation_type.equals("edit")) {
            try {
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
            }
        }
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
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
    }
}