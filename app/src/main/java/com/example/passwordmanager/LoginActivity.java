package com.example.passwordmanager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class LoginActivity extends Activity  {
    Button b1, b2, b3;
    EditText ed1, ed2;

    TextView tx1;
    int counter = 3;

    private String jsonTest = "[\n" +
            "\t{\n" +
            "\t\"type\": \"password\",\n" +
            "\t\"name\": \"moje pijrwsze hasło\",\n" +
            "\t\"data\": \"haslo1234\"\n" +
            "\t},\n" +
            "\t{\n" +
            "\t\"type\": \"directory\",\n" +
            "\t\"name\": \"mój pierwszy katalog\",\n" +
            "\t\"data\":[\n" +
            "\t\t\t{\n" +
            "\t\t\t\"type\": \"password\",\n" +
            "\t\t\t\"name\": \"hasło do pentagonu\",\n" +
            "\t\t\t\"data\": \"asdffdsa@\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\"type\": \"password\",\n" +
            "\t\t\t\"name\": \"hasło do konta bankowego\",\n" +
            "\t\t\t\"data\": \"a1!@#$%^&*\\\\\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\"type\": \"directory\",\n" +
            "\t\t\t\"name\": \"zagnieżdżony, pusty katlog\",\n" +
            "\t\t\t\"data\": []\n" +
            "\t\t\t}\n" +
            "\t\t]\n" +
            "\t}\n" +
            "]";

    public void login(View view) {
        Log.i("LoginActivity.login", "Creating intent");
        Intent intent = new Intent(this, MainActivity.class);
        Log.i("LoginActivity.login", "Intent created");
        EditText editText1 = (EditText) findViewById(R.id.editPassword_editName);
        String password = editText1.getText().toString();
        intent.putExtra("master_password", password);
        startActivity(intent);
    }

    public void moveToRegistration(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        String file = "filename";
        FileOutputStream fOut = null;
        try {
            fOut = openFileOutput(file, MODE_PRIVATE);
            fOut.write(jsonTest.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        b1 = (Button)findViewById(R.id.editPassword_saveButton);
        b2 = (Button)findViewById(R.id.editPassword_cancelButton);
        b3 = (Button)findViewById(R.id.main_logoutButton);
        ed1 = (EditText)findViewById(R.id.editPassword_editName);
        ed2 = (EditText)findViewById(R.id.editPassword_editPassword);

        tx1 = (TextView)findViewById(R.id.textView2);
        tx1.setVisibility(View.GONE);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(v);
                if(ed1.getText().toString().equals("admin") &&
                        ed2.getText().toString().equals("admin")) {
                    Toast.makeText(getApplicationContext(),
                            "Redirecting...",Toast.LENGTH_SHORT).show();
                    login(v);
                }else{
                    Toast.makeText(getApplicationContext(), "Wrong Credentials",Toast.LENGTH_SHORT).show();

                            tx1.setVisibility(View.VISIBLE);
                    tx1.setBackgroundColor(Color.RED);
                    counter--;
                    tx1.setText(Integer.toString(counter));

                    if (counter == 0) {
                        b1.setEnabled(false);
                    }
                }
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToRegistration(v);
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}