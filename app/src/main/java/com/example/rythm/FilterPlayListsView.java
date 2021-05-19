package com.example.rythm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FilterPlayListsView extends AppCompatActivity implements LibraryAdapter.onPlayListListener, SearchView.OnQueryTextListener {

    private List<Playlist> playlists;
    private SearchView svPlayListsFilter;
    private RecyclerView rvPlayListsFilter;
    private LibraryAdapter playListsFilterAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_play_lists_view);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        this.playlists = new ArrayList<>();



        getPlaylistsFromFirebase(currentUser.getUid());

        this.svPlayListsFilter = findViewById(R.id.svPlaylistFilter);
        this.rvPlayListsFilter = findViewById(R.id.rvPlayListsFilter);

        this.svPlayListsFilter.setOnQueryTextListener(this);
        this.playListsFilterAdapter = new LibraryAdapter(this.playlists, this, this);
        this.rvPlayListsFilter = findViewById(R.id.rvPlayListsFilter);
        this.rvPlayListsFilter.setHasFixedSize(true);
        this.rvPlayListsFilter.setLayoutManager(new LinearLayoutManager(this));
        this.rvPlayListsFilter.setAdapter(this.playListsFilterAdapter);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        this.playListsFilterAdapter.filter(newText);
        return false;
    }

    @Override
    public void onItemClick(int pos) {

    }

//    private void getPlaylistsFromFirebase(String userId, String query) {
//        Query songsQuery = db.collection("Playlists").whereEqualTo("userId", userId).whereGreaterThan("name", query);
//        songsQuery.addSnapshotListener((documentSnapshots, e) -> {
//            assert documentSnapshots != null;
//            for (DocumentChange doc: documentSnapshots.getDocumentChanges()){
//                if (doc.getType() == DocumentChange.Type.ADDED){
//                    QueryDocumentSnapshot document = doc.getDocument();
//                    String playlistId = document.getId(),
//                            name = (String) document.get("name");
//                    if (name != null && playlistId.length() > 0 && name.length() > 0) {
//                        this.playlists.add(new Playlist(name, playlistId));
//                    }
//                }
//            }
//        });
//    }

    private void getPlaylistsFromFirebase(String userId) {
        Query songsQuery = db.collection("Playlists").whereEqualTo("userId", userId);
        songsQuery.addSnapshotListener((documentSnapshots, e) -> {
            assert documentSnapshots != null;
            for (DocumentChange doc: documentSnapshots.getDocumentChanges()){
                if (doc.getType() == DocumentChange.Type.ADDED){
                    QueryDocumentSnapshot document = doc.getDocument();
                    String playlistId = document.getId(),
                            name = (String) document.get("name");
                    if (name != null && playlistId.length() > 0 && name.length() > 0) {
                        this.playlists.add(new Playlist(name, playlistId));
                    }
                }
            }
        });
    }
}