package com.example.rythm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginView extends AppCompatActivity {

    private Button btnLogin;
    private TextView btnSignUpLog;
    private EditText editEmailLogin,
                     editPWDLogin;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private ProgressBar progressBar;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_view);

        firebaseAuth = FirebaseAuth.getInstance();

        this.btnLogin = findViewById(R.id.btnLogin);
        this.btnSignUpLog = findViewById(R.id.btnSignUpLog);
        this.editEmailLogin = findViewById(R.id.editEmailLogin);
        this.editPWDLogin = findViewById(R.id.editPWDLogin);

        this.btnLogin.setOnClickListener(v -> {
            String  email = this.editEmailLogin.getText().toString().trim(),
                    password = this.editPWDLogin.getText().toString().trim();

            if (validateEmptyInputs(email, password)) {
                loginEmailPasswordUser(email, password);
            }
        });

        this.btnSignUpLog.setOnClickListener(v -> {
            Intent i = new Intent(getBaseContext(), SignUpView.class);
            startActivity(i);
            finish();
        });
    }

    public boolean validateEmptyInputs(String email, String password) {
        boolean flag = true;

        if (email.isEmpty()) {
            this.editEmailLogin.setError("Empty field");
            flag = false;
        }
        if (password.isEmpty()) {
            this.editPWDLogin.setError("Empty field");
            flag = false;
        }
        return flag;
    }


    private void loginEmailPasswordUser(String email, String password) {

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(task -> {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        assert user != null;
                        final String currentUserId = user.getUid();

                        collectionReference
                                .whereEqualTo("userId", currentUserId)
                                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                                    assert queryDocumentSnapshots != null;
                                    if (!queryDocumentSnapshots.isEmpty()) {

                                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                            Intent intent = new Intent(LoginView.this, HomeView.class);
                                            intent.putExtra("username", snapshot.getString("username"));
                                            intent.putExtra("userId", snapshot.getString("userId"));
                                            startActivity(intent);
                                            finish();
                                        }
                                    }

                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    });



        }
        else {
            Toast.makeText(LoginView.this,
                    "Please enter email and password",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }
}