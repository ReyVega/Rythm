package com.example.rythm;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class UserFragment extends Fragment {

    private static final String TAG_FRAGMENT = "fragment";
    private Button btnEditUserData;
    private EditUserDataFragment editUserDataFragment;

    public UserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        this.btnEditUserData = view.findViewById(R.id.btnEditUserData);
        this.editUserDataFragment = new EditUserDataFragment();

        this.btnEditUserData.setOnClickListener(v -> {
            FragmentManager mr = getFragmentManager();
            FragmentTransaction transaction = mr.beginTransaction();
            transaction.replace(R.id.container, this.editUserDataFragment, TAG_FRAGMENT);
            transaction.commit();
        });

        return view;
    }
}