package com.example.rythm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private Button btnCreate;
    private TextView LogInMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.btnCreate = findViewById(R.id.btnCreate);
        this.LogInMain = findViewById(R.id.logInMain);

        this.btnCreate.setOnClickListener(v -> {
            Intent i = new Intent(getBaseContext(), SignUpView.class);
            startActivity(i);
        });

        this.LogInMain.setOnClickListener(v -> {
            Intent i = new Intent(getBaseContext(), LoginView.class);
            startActivity(i);
        });

        //startActivity(new Intent(this, HomeView.class));
    }
}