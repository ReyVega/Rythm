package com.example.rythm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

public class HomeView extends AppCompatActivity {

    private static final String TAG_FRAGMENT = "fragment";
    private LibraryFragment libraryFragment;
    private UserFragment userFragment;
    private ImageView btnPlayLists,
            btnUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_view);

        this.btnPlayLists = findViewById(R.id.btnLibrary);
        this.btnUser = findViewById(R.id.btnUser);
        this.libraryFragment = new LibraryFragment();
        this.userFragment = new UserFragment();

        this.setFragment(libraryFragment);
        this.btnPlayLists.setOnClickListener(v -> {
            setFragment(this.libraryFragment);
        });

        this.btnUser.setOnClickListener(v -> {
            setFragment(this.userFragment);
        });
    }

    private void setFragment(Fragment fragment){
        FragmentManager mr = getSupportFragmentManager();
        FragmentTransaction transaction = mr.beginTransaction();
        transaction.replace(R.id.container, fragment, TAG_FRAGMENT);
        transaction.commit();
    }


}