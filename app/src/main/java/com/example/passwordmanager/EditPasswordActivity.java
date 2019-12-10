package com.example.passwordmanager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class EditPasswordActivity extends Activity {
    Button b1, b2, b3, b4;
    EditText ed1, ed2;
    String name, currentPath, masterPassword, operation_type, username, fullPath;
    LocalDatabase localDatabase = null;
    JSONArray database;
    CheckBox showPassword;


    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void saveChanges(String path) throws JSONException {
        JSONArray directory = localDatabase.getDirectory(currentPath).getJSONArray("data");
        if (operation_type.equals("edit")) {
            JSONObject newPassword = new JSONObject(localDatabase.getPassword(fullPath).toString());
            newPassword.put("name", ed1.getText().toString());
            newPassword.put("data", ed2.getText().toString());
            localDatabase.safeModify(fullPath, newPassword);
            /*for (int i = 0; i < directory.length(); i++) {
                JSONObject item = directory.getJSONObject(i);
                if (item.optString("type").equals("password") && item.optString("name").equals(name)) {
                    item.put("name", ed1.getText().toString());
                    item.put("data", ed2.getText().toString());
                    break;
                }
            }*/
        } else if (operation_type.equals("delete")) {
            Log.i("TESTdeletepass", fullPath);
            localDatabase.deletePassword(fullPath);
            /*for (int i = 0; i < directory.length(); i++) {
                JSONObject item = directory.getJSONObject(i);
                if (item.optString("type").equals("password") && item.optString("name").equals(name)) {
                    directory.remove(i);
                    break;
                }
            }*/
        } else {
            JSONObject newItem = new JSONObject();
            newItem.put("type", "password");
            newItem.put("name", ed1.getText().toString());
            newItem.put("data", ed2.getText().toString());
            localDatabase.safeAdd(currentPath, newItem);
            //directory.put(newItem);
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


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);


        b1 = (Button)findViewById(R.id.editPassword_saveButton);
        b2 = (Button)findViewById(R.id.editPassword_cancelButton);
        b3 = (Button) findViewById(R.id.editPassword_deleteButton);
        b4 = (Button) findViewById(R.id.editPassword_generateButton);
        ed1 = (EditText)findViewById(R.id.editPassword_editName);
        ed2 = (EditText)findViewById(R.id.editPassword_editPassword);
        showPassword = (CheckBox)findViewById(R.id.editPassword_showPassword);
        Intent intent = getIntent();
        //String databaseString = intent.getStringExtra("database");
        username = intent.getStringExtra("username");
        currentPath = intent.getStringExtra("path");
        operation_type = intent.getStringExtra("operation_type");
        localDatabase = LocalDatabase.getInstance(username);
        if (!operation_type.equals("edit")) {
            ((TextView) findViewById(R.id.editPassword_textview)).setText("CREATE PASSWORD");
            b3.setVisibility(View.GONE);
        } else{
            name = intent.getStringExtra("name");
            fullPath = currentPath+name;
        }
        /*try {
            database = new org.json.JSONArray(databaseString);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        if (operation_type.equals("edit")){
            JSONObject item = localDatabase.getPassword(fullPath);
            ed1.setText(item.optString("name"));
            ed2.setText(item.optString("data"));
            /*try {
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
            }*/
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
                    ((TextView) findViewById(R.id.editPassword_editPassword)).setTransformationMethod(null);
                } else {
                    ((TextView) findViewById(R.id.editPassword_editPassword)).setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
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

        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LayoutInflater inflater = (LayoutInflater)
                            getSystemService(LAYOUT_INFLATER_SERVICE);

                    View popupView = inflater.inflate(R.layout.generate_window, null);

                    // create the popup window
                    int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                    int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    boolean focusable = true; // lets taps outside the popup also dismiss it
                    final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                    final EditText et = (EditText) popupView.findViewById(R.id.editText);
                    Button gen = (Button) popupView.findViewById(R.id.button);
                    Button cancel = (Button) popupView.findViewById(R.id.button2);
                    final CheckBox digits = ((CheckBox) popupView.findViewById(R.id.checkBox));
                    final CheckBox lower = ((CheckBox) popupView.findViewById(R.id.checkBox2));
                    final CheckBox upper = ((CheckBox) popupView.findViewById(R.id.checkBox3));
                    final CheckBox punctuation = ((CheckBox) popupView.findViewById(R.id.checkBox4));
                    gen.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (digits.isChecked() || lower.isChecked() || upper.isChecked() || punctuation.isChecked()) {
                                PasswordGenerator passwordGenerator = new PasswordGenerator.PasswordGeneratorBuilder()
                                        .useDigits(digits.isChecked())
                                        .useLower(lower.isChecked())
                                        .useUpper(upper.isChecked())
                                        .usePunctuation(punctuation.isChecked())
                                        .build();
                                String password = passwordGenerator.generate(Integer.parseInt(et.getText().toString()));
                                ((EditText) findViewById(R.id.editPassword_editPassword)).setText(password);
                                popupWindow.dismiss();
                            } else {
                                Toast.makeText(getApplicationContext(), "Choose at least one option", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                        }
                    });
                    // show the popup window
                    // which view you pass in doesn't matter, it is only used for the window tolken
                    popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
