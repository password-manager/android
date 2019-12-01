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

public class RegisterActivity extends Activity {
    Button b1, b2, b3;
    EditText ed1, ed2;
    CheckBox showPassword;
    public byte[] hashPassword(String password, byte[] salt2){
        int iterations = 100000;
        int keyLength = 512;
        char[] passwordChars = password.toCharArray();
        byte[] res2;
        try
        {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec2 = new PBEKeySpec( passwordChars, salt2, iterations, keyLength );
            SecretKey key2 = keyFactory.generateSecret( spec2 );
            res2 = key2.getEncoded( );
            String hashString2 = new String(res2);
            String saltString2 = new String(salt2);
            Log.i("TESTTT hash and salt", Base64.encodeToString(res2,0)+"\n"+saltString2);
            return res2;
        }
        catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        b1 = (Button)findViewById(R.id.register_registerButton);
        b2 = (Button)findViewById(R.id.register_cancelButton);
        ed1 = (EditText)findViewById(R.id.register_editEmail);
        ed2 = (EditText)findViewById(R.id.register_editPassword);
        showPassword = (CheckBox)findViewById(R.id.register_showPassword);

        //tx1 = (TextView)findViewById(R.id.textView2);
        //tx1.setVisibility(View.GONE);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = ed1.getText().toString();
                byte[] salt = {1};  //TODO
                byte[] passwordHash = hashPassword(ed2.getText().toString(), salt);
                User user = new User(email, passwordHash, salt);
                //if connected register in server
                Userbase userbase = Userbase.getInstance(getBaseContext());
                if (userbase.getUser(email) == null){
                    userbase.saveUser(user);
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
                    ((TextView) findViewById(R.id.register_editPassword)).setTransformationMethod(null);
                } else {
                    ((TextView) findViewById(R.id.register_editPassword)).setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });
    }
}
