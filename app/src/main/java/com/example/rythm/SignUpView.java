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

public class SignUpView extends AppCompatActivity {

    private Button btnSignUp;
    private TextView btnLogInSign;
    private EditText username,
                     editEmailSignUp,
                     editPWDSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_view);

        this.btnSignUp = findViewById(R.id.btnSignUp);
        this.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), HomeView.class);
                if (validationSignUp()) {
                    startActivity(i);
                    finish();
                }
            }
        });

        this.btnLogInSign = findViewById(R.id.btnLogInSign);
        this.btnLogInSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), LoginView.class);
                startActivity(i);
                finish();
            }
        });

        this.username = findViewById(R.id.username);
        this.editEmailSignUp = findViewById(R.id.editEmailSignUp);
        this.editPWDSignUp = findViewById(R.id.editPWDSignUp);
    }

    public boolean validationSignUp() {
        if (this.username.getText().toString().trim().isEmpty() || this.editEmailSignUp.getText().toString().isEmpty() ||
                this.editPWDSignUp.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Fill all the fields", Toast.LENGTH_LONG).show();
        } else {
            if(Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$").matcher(
                    this.editEmailSignUp.getText().toString().trim()).find()) {
                return true;
            } else {
                Toast.makeText(getApplicationContext(), "Enter a valid e-mail", Toast.LENGTH_LONG).show();
            }
        }
        return false;
    }
}