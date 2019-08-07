package com.example.passwordmanager;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class RegisterActivity extends Activity {
    Button b1, b2, b3;
    EditText ed1, ed2;
    CheckBox showPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        b1 = (Button)findViewById(R.id.register_registerButton);
        b2 = (Button)findViewById(R.id.register_cancelButton);
        ed1 = (EditText)findViewById(R.id.register_editEmail);
        showPassword = (CheckBox)findViewById(R.id.register_showPassword);

        //tx1 = (TextView)findViewById(R.id.textView2);
        //tx1.setVisibility(View.GONE);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //register
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
