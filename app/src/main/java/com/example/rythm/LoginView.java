package com.example.rythm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

public class LoginView extends AppCompatActivity {

    private Button btnLogin;
    private TextView btnSignUpLog;
    private EditText editEmailLogin,
                     editPWDLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_view);

        this.btnLogin = findViewById(R.id.btnLogin);
        this.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), HomeView.class);
                if (validationLogin()) {
                    startActivity(i);
                    finish();
                }
            }
        });

        this.btnSignUpLog = findViewById(R.id.btnSignUpLog);
        this.btnSignUpLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), SignUpView.class);
                startActivity(i);
                finish();
            }
        });

        this.editEmailLogin = findViewById(R.id.editEmailLogin);
        this.editPWDLogin = findViewById(R.id.editPWDLogin);
    }

    public boolean validationLogin() {
        if (this.editEmailLogin.getText().toString().trim().isEmpty() || this.editPWDLogin.getText().toString().trim().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Fill all the fields", Toast.LENGTH_LONG).show();
        } else {
            if(Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$").matcher(
                    this.editEmailLogin.getText().toString().trim()).find()) {
                return true;
            } else {
                Toast.makeText(getApplicationContext(), "Enter a valid e-mail", Toast.LENGTH_LONG).show();
            }
        }
        return false;
    }
}