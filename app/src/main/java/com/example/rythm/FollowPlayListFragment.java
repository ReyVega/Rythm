package com.example.rythm;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FollowPlayListFragment extends Fragment implements FollowPlayListAdapter.onSongListener {

    private FollowPlayListAdapter playListAdapter;

    private static final String TAG_FRAGMENT = "fragment";
    private List<Song> songs;
    private String playListName,
            imageURL,
            author;
    private boolean isAdded;
    private RecyclerView recyclcerViewSongs;

    private RequestQueue queue;

    private View view;

    //Connection to Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference songsCollectionReference = db.collection("Songs"),
                                playlistsCollectionReference = db.collection("Playlists");


    private String playlistId;

    public FollowPlayListFragment() {
        // Required empty public constructor
    }

    public FollowPlayListFragment(String playListName, boolean isAdded) {
        this.playListName = playListName;
        this.isAdded = isAdded;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_follow_play_list, container, false);
        this.getPlaylistAuthor(this.playlistId);

        this.songs = new ArrayList<>();
        this.playListAdapter = new FollowPlayListAdapter(this.songs, getContext(), this);
        getSongsFromFirebase();
        return view;
    }

    @Override
    public void onSongClick(int pos) {
        Intent i = new Intent(getContext(), SongView.class);
        i.putExtra("playlistPosition", pos - 1);
        i.putExtra("playlistId", this.playlistId);
        i.putExtra("playlistName", playListName);

        startActivity(i);
    }

    private void getPlaylistAuthor(String playlistId) {
        DocumentReference documentReference = playlistsCollectionReference.document(playlistId);

        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document == null) return;
                if (document.exists()) {
                    String authorId = document.getString("userId");

                    db.collection("Users")
                            .whereEqualTo("userId", authorId)
                            .get()
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    for (QueryDocumentSnapshot document1 : Objects.requireNonNull(task1.getResult())) {
                                        author = Objects.requireNonNull(document1.getData().get("username")).toString();
                                        playListAdapter.setFm(getParentFragmentManager());
                                        playListAdapter.setPlayListName(playListName);
                                        playListAdapter.setPlayListID(playlistId);
                                        playListAdapter.setPlayListImage(imageURL);
                                        playListAdapter.setAuthor(author);
                                        playListAdapter.setAdded(isAdded);

                                        recyclcerViewSongs = view.findViewById(R.id.rvFollowPlayList);
                                        recyclcerViewSongs.setHasFixedSize(true);
                                        recyclcerViewSongs.setLayoutManager(new LinearLayoutManager(view.getContext()));
                                        recyclcerViewSongs.setAdapter(playListAdapter);
                                    }
                                } else {
                                    Log.w("Error", "Error getting documents.", task1.getException());
                                }
                            });
                } else {
                    Toast.makeText(getContext(), "Playlist not found", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getContext(), "Playlist not found", Toast.LENGTH_LONG).show();

            }
        });


    }

    private void getSongsFromFirebase() {
        Query songsQuery = db.collection("Songs")
                .whereEqualTo("playlistId", playlistId)
                .orderBy("addedTimestamp")
                .orderBy("deezerTrackId");

        this.playListAdapter.addSong(new Song());
        songsQuery.addSnapshotListener((documentSnapshots, e) -> {
            if (documentSnapshots == null) return;
            for (DocumentChange doc: documentSnapshots.getDocumentChanges()){
                if (doc.getType() == DocumentChange.Type.ADDED){
                    String deezerTrackId = String.valueOf(doc.getDocument().get("deezerTrackId"));
                    Log.d("TOMATE", "getSongsFromFirebase: track id" + deezerTrackId);
                    this.playListAdapter.addSong(new Song());
                    fetchSongMetadata(deezerTrackId, playListAdapter.getItemCount() - 1);
                }
            }
        });
    }

    private void fetchSongMetadata(String deezerTrackId, int pos) {
        if (!isAdded()) return; // Avoids exception
        this.queue = RequestController.getInstance(getContext()).getRequestQueue();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                getString(R.string.track_endpoint_deezer_api) + deezerTrackId, null,
                track -> {
                    try {
                        String songName = track.getString("title");
                        JSONObject artist = track.getJSONObject("artist");
                        String artistName = artist.getString("name");
                        int duration = track.getInt("duration");
                        JSONObject album = track.getJSONObject("album");
                        String coverUrl = album.getString("cover");
                        this.playListAdapter.setSong(new Song(songName, artistName, duration, coverUrl, deezerTrackId), pos);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.d("JSON", "onErrorResponse: " + error.getMessage()));

        queue.add(jsonObjectRequest);
    }

    public void setFragment(Fragment fragment) {
        FragmentManager mr = getParentFragmentManager();
        FragmentTransaction transaction = mr.beginTransaction();
        transaction.replace(R.id.container, fragment, TAG_FRAGMENT);
        transaction.commit();
    }

    public void setImagePlayList(String imageURL) {
        this.imageURL = imageURL;
    }
}