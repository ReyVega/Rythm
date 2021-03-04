package com.example.rythm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SignUpView extends AppCompatActivity {

    private Button btnSignUp;
    private TextView btnLogInSign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_view);

        this.btnSignUp = findViewById(R.id.btnSignUp);
        this.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), HomeView.class);
                startActivity(i);
                finish();
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
    }
}