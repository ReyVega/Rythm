package com.example.rythm;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONException;
import org.json.JSONObject;

public class PlayListFragment extends Fragment implements PlayListAdapter.onSongListener {

    private static final String TAG_FRAGMENT = "fragment";
    private List<Song> songs;
    private ImageView btnAddSong;
    private TextView tvPlayListName;
    private PlayListAdapter playListAdapter;
    private SearchAddSongFragment searchAddSongFragment;
    private String playListName;

    public PlayListFragment(String playListName) {
        this.playListName = playListName;
    }

    private RequestQueue queue;

    //Connection to Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String playlistId;

    private RequestQueue queue;

    //Connection to Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    private String playlistId;

    public PlayListFragment() {
        // Required empty public constructor
    }

    public PlayListFragment(String playlistId) {
        this.playlistId = playlistId;
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
        View view = inflater.inflate(R.layout.fragment_play_list, container, false);

        this.searchAddSongFragment = new SearchAddSongFragment();
        this.btnAddSong = view.findViewById(R.id.btnAddSong);
        this.tvPlayListName = view.findViewById(R.id.tvPlayListName);
        this.tvPlayListName.setText(this.playListName);
        this.btnAddSong.setOnClickListener(v -> {
            FragmentManager mr = getFragmentManager();
            FragmentTransaction transaction = mr.beginTransaction();
            transaction.replace(R.id.container, this.searchAddSongFragment, TAG_FRAGMENT);
            transaction.commit();
        });
        getSongsFromFirebase();

        this.songs = new ArrayList<>();

        this.playListAdapter = new PlayListAdapter(this.songs, view.getContext(), this);
        RecyclerView rv = view.findViewById(R.id.recyclerViewSongs);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(view.getContext()));
        rv.setAdapter(this.playListAdapter);
        return view;
    }

    @Override
    public void onSongClick(int pos) {
        Intent i = new Intent(getContext(), SongView.class);
        startActivity(i);
    }

    private void getSongsFromFirebase() {
        Query songsQuery = db.collection("Songs").whereEqualTo("playlistId", playlistId);
        songsQuery.addSnapshotListener((documentSnapshots, e) -> {
            assert documentSnapshots != null;
            for (DocumentChange doc: documentSnapshots.getDocumentChanges()){
                if (doc.getType() == DocumentChange.Type.ADDED){
                    String deezerTrackId = String.valueOf(doc.getDocument().get("deezerTrackId"));
                    Log.d("TOMATE", "getSongsFromFirebase: track id" + deezerTrackId);
                    fetchSongMetadata(deezerTrackId);
                }
            }
        });
    }

    private void fetchSongMetadata(String deezerTrackId) {
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
                        this.playListAdapter.addSong(new Song(songName, artistName, duration, coverUrl, "123"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.d("JSON", "onErrorResponse: " + error.getMessage()));

        queue.add(jsonObjectRequest);
    }
}