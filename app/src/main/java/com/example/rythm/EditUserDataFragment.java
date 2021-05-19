package com.example.rythm;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;


public class EditUserDataFragment extends Fragment {

    private FirebaseUser user;
    private TextView tvUsername;
    private EditText editUserData,
                     editPWDUserData,
                     editPWDConfirmationUserData;
    private Button btnEditUserData;


    public EditUserDataFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_user_data, container, false);
        this.user = FirebaseAuth.getInstance().getCurrentUser();

        this.tvUsername = view.findViewById(R.id.tvUsername);
        this.editUserData = view.findViewById(R.id.editUserData);
        this.editPWDUserData = view.findViewById(R.id.editPWDUserData);
        this.editPWDConfirmationUserData = view.findViewById(R.id.editPWDConfirmationUserData);
        this.btnEditUserData = view.findViewById(R.id.btnEditUserData);

        this.btnEditUserData.setOnClickListener(v -> {
            String  userData = this.editUserData.getText().toString().trim(),
                    password = this.editPWDUserData.getText().toString().trim(),
                    confirmationPWD = this.editPWDConfirmationUserData.getText().toString().trim();

            if (validateEmptyInputs(userData, password, confirmationPWD)) {
                if (validatePWDCoincidence(password,confirmationPWD)) {
                    updateUserData(userData, password);
                }
            } else {
                Toast.makeText(getContext(), "Fill all the fields", Toast.LENGTH_SHORT).show();
            }
        });

        this.tvUsername.setText(this.user.getDisplayName());

        return view;
    }

    public boolean validateEmptyInputs(String email, String password, String confirmationPWD) {
        boolean flag = true;

        if (email.isEmpty()) {
            this.editUserData.setError("Empty field");
            flag = false;
        }
        if (password.isEmpty()) {
            this.editPWDUserData.setError("Empty field");
            flag = false;
        }
        if (confirmationPWD.isEmpty()) {
            this.editPWDConfirmationUserData.setError("Empty field");
            flag = false;
        }
        return flag;
    }

    public boolean validatePWDCoincidence(String PWD, String ConfirmationPWD) {
        if (PWD.equals(ConfirmationPWD)) {
            return true;
        } else {
            Toast.makeText(getContext(), "Passwords are not the same", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void updateUserData(String newUsername, String newPWD) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newUsername)
                .build();

        this.user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Success", "User profile updated.");
                            tvUsername.setText(newUsername);
                        }
                    }
                }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        });

        this.user.updatePassword(newPWD)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Success", "User password updated.");
                        }
                    }
                }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        });
        Toast.makeText(getContext(), "User data updated", Toast.LENGTH_SHORT).show();
    }
}