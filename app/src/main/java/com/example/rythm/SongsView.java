package com.example.rythm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class SongsView extends AppCompatActivity {

    private static final String TAG = "SongView";
    private List<Song> songs;
    private FloatingActionButton addSong;

    //Connection to Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference songsCollectionReference = db.collection("Songs");

    private TextView textGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs_view);

        Intent intent = getIntent();


        final String    username = intent.getStringExtra("username"),
                userId = intent.getStringExtra("userId");

        this.textGreeting = findViewById(R.id.textGreeting);
        this.textGreeting.setText(String.format("Hi, %s!", username));

        this.addSong = findViewById(R.id.addSong);
        this.addSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), SongForm.class);
                i.putExtra("userId", userId);
                startActivity(i);
            }
        });

    }

    private void getSongs(String userId) {
        Query songsQuery = db.collection("Songs").whereEqualTo("userId", userId).orderBy("songName");
        songsQuery.addSnapshotListener((documentSnapshots, e) -> {
            songs = new ArrayList<Song>();
            Song currSong;

            assert documentSnapshots != null;
            for (DocumentChange doc: documentSnapshots.getDocumentChanges()){
                if (doc.getType() == DocumentChange.Type.ADDED){
                    currSong =  doc.getDocument().toObject(Song.class);
                    songs.add(currSong);
                }
            }
            ListAdapter la = new ListAdapter(songs, SongsView.this);
            RecyclerView rv = findViewById(R.id.recyclerView);
            rv.setHasFixedSize(true);
            rv.setLayoutManager(new LinearLayoutManager(SongsView.this));
            rv.setAdapter(la);
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        final String userId = intent.getStringExtra("userId");
        this.getSongs(userId);
    }
}