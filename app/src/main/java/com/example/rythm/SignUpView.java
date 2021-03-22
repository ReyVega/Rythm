package com.example.rythm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class SignUpView extends AppCompatActivity {

    private Button btnSignUp;
    private TextView btnLogInSign;
    private EditText username,
                     editEmailSignUp,
                     editPWDSignUp;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //Firestore connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_view);

        firebaseAuth = FirebaseAuth.getInstance();

        this.btnSignUp = findViewById(R.id.btnSignUp);
        this.btnLogInSign = findViewById(R.id.btnLogInSign);
        this.username = findViewById(R.id.username);
        this.editEmailSignUp = findViewById(R.id.editEmailSignUp);
        this.editPWDSignUp = findViewById(R.id.editPWDSignUp);

        this.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String  userName = username.getText().toString().trim(),
                        email = editEmailSignUp.getText().toString().trim(),
                        password = editPWDSignUp.getText().toString().trim();

                if (validateEmptyInputs(userName, email, password)) {
                    createUserEmailAccount(userName, email, password);
                }
            }
        });

        this.btnLogInSign.setOnClickListener(v -> {
            Intent i = new Intent(getBaseContext(), LoginView.class);
            startActivity(i);
            finish();
        });
    }

    public boolean validateEmptyInputs(String username, String email, String password) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Fill all the fields", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    private void createUserEmailAccount(String username, String email, String password) {
        if (!TextUtils.isEmpty(email)
                && !TextUtils.isEmpty(password)
                && !TextUtils.isEmpty(username)) {
            Log.d("SignUp", "createUserEmailAccount: " + email + " " + password + " " + username);

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            //we take user to AddJournalActivity
                            currentUser = firebaseAuth.getCurrentUser();
                            assert currentUser != null;
                            final String currentUserId = currentUser.getUid();

                            //Create a user Map so we can create a user in the User collection
                            Map<String, String> userObj = new HashMap<>();
                            userObj.put("userId", currentUserId);
                            userObj.put("username", username);

                            //save to our firestore database
                            collectionReference.add(userObj)
                                    .addOnSuccessListener(documentReference -> documentReference.get()
                                            .addOnCompleteListener(task1 -> {
                                                if (Objects.requireNonNull(task1.getResult()).exists()) {
                                                    Intent intent = new Intent(SignUpView.this,
                                                            SongsView.class);
                                                    intent.putExtra("username", username);
                                                    intent.putExtra("userId", currentUserId);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }))
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getApplicationContext(), "User can not be added to DB", Toast.LENGTH_LONG).show();
                                    });
                        }

                    })
                    .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }
}