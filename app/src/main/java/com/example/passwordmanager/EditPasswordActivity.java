package com.example.passwordmanager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class EditPasswordActivity extends Activity {
    Button b1, b2, b3;
    EditText ed1, ed2;
    String name, currentPath, masterPassword, operation_type;
    JSONArray database;
    CheckBox showPassword;
    TextView tx1;
    int counter = 3;

    public JSONArray getDirectory(String path) throws JSONException {
        if (path.equals("/")) return database;
        String[] pathArray = Arrays.copyOfRange(path.split("/"), 1, path.split("/").length);
        JSONArray directory = database;
        for (String directoryName : pathArray){
            for (int i = 0; i < directory.length(); i++){
                JSONObject item = directory.getJSONObject(i);
                if (item.optString("type").equals("directory") && item.optString("name").equals(directoryName)){
                    directory = item.getJSONArray("data");
                    break;
                }
            }
        }
        return directory;
    }

    public void saveChanges(String path) throws JSONException {
        JSONArray directory = database;
        if (!path.equals("/")){
            String[] pathArray = Arrays.copyOfRange(path.split("/"), 1, path.split("/").length);
            for (String directoryName : pathArray){
                for (int i = 0; i < directory.length(); i++){
                    JSONObject item = directory.getJSONObject(i);
                    if (item.optString("type").equals("directory") && item.optString("name").equals(directoryName)){
                        directory = item.getJSONArray("data");
                        break;
                    }
                }
            }
        }
        if (operation_type.equals("edit")) {
            for (int i = 0; i < directory.length(); i++) {
                JSONObject item = directory.getJSONObject(i);
                if (item.optString("type").equals("password") && item.optString("name").equals(name)) {
                    item.put("name", ed1.getText().toString());
                    item.put("data", ed2.getText().toString());
                    break;
                }
            }
        } else {
            JSONObject newItem = new JSONObject();
            newItem.put("type", "password");
            newItem.put("name", ed1.getText().toString());
            newItem.put("data", ed2.getText().toString());
            directory.put(newItem);
        }
        saveDatabase();
        finish();
    }

    private void saveDatabase() {
        String file = "filename";
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
        setContentView(R.layout.activity_create);

        b1 = (Button)findViewById(R.id.button1);
        b2 = (Button)findViewById(R.id.button2);
        ed1 = (EditText)findViewById(R.id.editText1);
        ed2 = (EditText)findViewById(R.id.editText2);
        showPassword = (CheckBox)findViewById(R.id.checkBox);
        Intent intent = getIntent();
        String databaseString = intent.getStringExtra("database");
        currentPath = intent.getStringExtra("path");
        operation_type = intent.getStringExtra("operation_type");
        name = intent.getStringExtra("name");
        try {
            database = new org.json.JSONArray(databaseString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (operation_type.equals("edit")){
            try {
                JSONArray directory = getDirectory(currentPath);
                for (int i = 0; i < directory.length(); i++) {
                    JSONObject item = directory.getJSONObject(i);
                    if (item.optString("type").equals("password") && item.optString("name").equals(name)) {
                        //password = item.getJSONArray("data");
                        ed1.setText(item.optString("name"));
                        ed2.setText(item.optString("data"));
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
        showPassword.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //is chkIos checked?
                if (((CheckBox) v).isChecked()) {
                    ((TextView) findViewById(R.id.editText2)).setTransformationMethod(null);
                } else {
                    ((TextView) findViewById(R.id.editText2)).setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });
    }
}
