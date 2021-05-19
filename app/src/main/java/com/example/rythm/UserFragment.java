package com.example.rythm;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class UserFragment extends Fragment {

    private static final String TAG_FRAGMENT = "fragment";
    private Button btnEditUserData;
    private EditUserDataFragment editUserDataFragment;
    private TextView tvUsername,
                     tvUserEmail;
    private FirebaseUser user;


    public UserFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        this.user = FirebaseAuth.getInstance().getCurrentUser();

        this.tvUsername = view.findViewById(R.id.tvUsername);
        this.tvUserEmail = view.findViewById(R.id.tvUserEmail);
        this.btnEditUserData = view.findViewById(R.id.btnEditUserData);
        this.editUserDataFragment = new EditUserDataFragment();

        this.btnEditUserData.setOnClickListener(v -> {
            FragmentManager mr = getFragmentManager();
            FragmentTransaction transaction = mr.beginTransaction();
            transaction.replace(R.id.container, this.editUserDataFragment, TAG_FRAGMENT);
            transaction.commit();
        });

        this.tvUsername.setText(this.user.getDisplayName());
        this.tvUserEmail.setText(this.user.getEmail());

        return view;
    }
}