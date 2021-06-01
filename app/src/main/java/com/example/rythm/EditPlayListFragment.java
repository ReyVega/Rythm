package com.example.rythm;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class EditPlayListFragment extends Fragment {

    private static final int PICK_IMAGE = 100;
    private Uri imageUri;
    private ImageView ivPlayList;
    private Button btnSavePlayListData;

    public EditPlayListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_play_list, container, false);
        this.btnSavePlayListData = view.findViewById(R.id.btnSavePlayListData);
        this.ivPlayList = view.findViewById(R.id.ivPlayList);

        this.ivPlayList.setOnClickListener(v -> {
            Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            ActivityResultLauncher.launch(gallery);
        });
        this.btnSavePlayListData.setOnClickListener(v -> {

        });
        return view;
    }

    ActivityResultLauncher<Intent> ActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    this.imageUri = data.getData();
                    this.ivPlayList.setImageURI(this.imageUri);
                }
            });
}