package com.example.passwordmanager;

import android.app.Activity;
import android.content.Intent;
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

public class RegisterActivity extends Activity {
    Button b1, b2, b3;
    EditText ed1, ed2;
    CheckBox showPassword;
    ServerConnection serverConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        b1 = (Button)findViewById(R.id.register_registerButton);
        b2 = (Button)findViewById(R.id.register_cancelButton);
        ed1 = (EditText)findViewById(R.id.register_editEmail);
        ed2 = (EditText)findViewById(R.id.register_editPassword);
        showPassword = (CheckBox)findViewById(R.id.register_showPassword);
        serverConnection = ServerConnection.getInstance();
        //tx1 = (TextView)findViewById(R.id.textView2);
        //tx1.setVisibility(View.GONE);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = ed1.getText().toString();
                byte[] salt = {1};  //TODO
                byte[] passwordHash = Cryptography.hashPassword(ed2.getText().toString(), salt);
                User user = new User(email, passwordHash, salt);
                //if connected register in server
                try {
                    serverConnection.register(email.trim(), ed2.getText().toString().trim(), Base64.encodeToString(salt, Base64.DEFAULT));
                    Log.i("TEST", ServerConnection.result);
                    //if (ServerConnection.result.split(":")[1].equals("notOk")){
                    //    return;
                    //}
                    Userbase userbase = Userbase.getInstance(getBaseContext());
                    if (userbase.getUser(email) == null){
                        userbase.saveUser(user);
                    }
                    Intent intent = new Intent(getApplicationContext(), VerifyActivity.class);
                    String password = ed2.getText().toString();
                    String username = ed1.getText().toString();
                    intent.putExtra("master_password", password);
                    intent.putExtra("username", username);
                    startActivity(intent);
                } catch (InterruptedException e) {
                    return;
                    //e.printStackTrace();
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
                    ((TextView) findViewById(R.id.register_editPassword)).setTransformationMethod(null);
                } else {
                    ((TextView) findViewById(R.id.register_editPassword)).setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });
    }
}
