package com.example.rythm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SongForm extends AppCompatActivity {

    private static final String TAG = "SongForm";
    private Button btnSongCancel,
                   btnSongSave;
    private Spinner spGenre;
    private EditText    editSongName,
                        editArtistName;

    //Connection to Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference songsCollectionReference = db.collection("Songs");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_form);

        this.btnSongCancel = findViewById(R.id.btnSongCancel);
        this.btnSongSave = findViewById(R.id.btnSaveSong);
        this.spGenre = findViewById(R.id.genreMusic);
        this.editSongName = findViewById(R.id.editSongName);
        this.editArtistName = findViewById(R.id.editArtistName);

        this.btnSongCancel.setOnClickListener(v -> finish());

        this.btnSongSave.setOnClickListener(v -> {
            String  songName = this.editSongName.getText().toString().trim(),
                    artistName = this.editArtistName.getText().toString().trim(),
                    genre = this.spGenre.getSelectedItem().toString().trim();

            if (songName.length() == 0 || artistName.length() == 0 || genre.length() == 0) {
                Toast.makeText(SongForm.this,
                        "Fill all the fields!",
                        Toast.LENGTH_LONG)
                        .show();
                return;
            }

            Song song = new Song(songName, artistName, genre);


            songsCollectionReference.add(song)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(SongForm.this,
                                    "Song added successfully",
                                    Toast.LENGTH_LONG)
                                    .show();
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SongForm.this,
                                    "Song can not be added",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_spinner,getResources().getStringArray(R.array.list));
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        this.spGenre.setAdapter(adapter);
    }
}