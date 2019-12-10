package com.example.passwordmanager;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class VerifyActivity extends Activity {
    Button b1, b2, b3;
    EditText ed1, ed2;
    CheckBox showPassword;
    ServerConnection serverConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        b1 = (Button)findViewById(R.id.verify_button);
        b2 = (Button)findViewById(R.id.verify_cancel);
        ed1 = (EditText)findViewById(R.id.verify_editText);
        showPassword = (CheckBox)findViewById(R.id.verify_showPassword);
        serverConnection = ServerConnection.getInstance();
        final String password = getIntent().getExtras().getString("master_password");
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    serverConnection.sendCode(password.trim(), ed1.getText().toString().trim());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();
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
                    ((TextView) findViewById(R.id.verify_editText)).setTransformationMethod(null);
                } else {
                    ((TextView) findViewById(R.id.verify_editText)).setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });
    }
}