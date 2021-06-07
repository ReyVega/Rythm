package com.example.rythm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class EditPlayListFragment extends Fragment {

    private static final int PICK_IMAGE = 100;
    private Uri imageUri;
    private ImageView ivPlayList;
    private EditText editPlayList;
    private Button btnSavePlayListData;
    private String playListID;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private StorageTask uploadTask;

    private String imageURL = "";

    public EditPlayListFragment() {

    }

    public EditPlayListFragment(String playListID) {
        this.playListID = playListID;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fetchPlayList(this.playListID);

        View view = inflater.inflate(R.layout.fragment_edit_play_list, container, false);
        this.btnSavePlayListData = view.findViewById(R.id.btnSavePlayListData);
        this.ivPlayList = view.findViewById(R.id.ivPlayList);
        this.editPlayList = view.findViewById(R.id.editPlayList);

        this.ivPlayList.setOnClickListener(v -> {
            Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            ActivityResultLauncher.launch(gallery);
        });
        this.btnSavePlayListData.setOnClickListener(v -> {
            if (!this.editPlayList.getText().toString().trim().isEmpty()) {
                this.updatePlayList();
            } else {
                this.editPlayList.setError("Empty field");
            }
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


    public void updatePlayList() {
        DocumentReference docRef = db.collection("Playlists").document(this.playListID);
        if (this.imageUri != null) {
            docRef.update("name", this.editPlayList.getText().toString().trim());
            uploadImageToFirestore(docRef);
        } else {
            docRef.update("name", this.editPlayList.getText().toString().trim());
        }

        Toast.makeText(getContext(), "Playlist edited succesfully!", Toast.LENGTH_SHORT).show();
    }


    public void uploadImageToFirestore(DocumentReference docRef) {
        StorageReference riversRef = storageRef.child("PlaylistImages/" + this.playListID);
        uploadTask = riversRef.putFile(this.imageUri);
        uploadTask.addOnFailureListener(exception -> {
            Toast.makeText(getContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
        }).addOnSuccessListener((OnSuccessListener<UploadTask.TaskSnapshot>) taskSnapshot -> {
            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!urlTask.isSuccessful());
            Uri downloadUrl = urlTask.getResult();
            docRef.update("imageURL", downloadUrl.toString());
        });
    }

    public void fetchPlayList(String playListID)  {
        DocumentReference docRef = db.collection("Playlists").document(this.playListID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    this.imageURL = document.get("imageURL").toString();

                    if (!this.imageURL.equals("")) {
                        Picasso.with(getContext()).load(this.imageURL).into(this.ivPlayList);
                    }
                }
            }
        });
    }
}