package com.example.rythm;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FilterSongsFragment extends Fragment implements SongsAdapter.onSongListener, SearchView.OnQueryTextListener {

    private static final String TAG_FRAGMENT = "fragment";
    private List<Song> songs;
    private SearchView svSongsFilter;
    private RecyclerView rvSongsFilter;
    private SongsAdapter songsAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String playlistID;
    private RequestQueue queue;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private final int waitingTime = 200;

    public FilterSongsFragment() {

    }

    public FilterSongsFragment(String playListID) {
        this.playlistID = playListID;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_filter_songs_view, container, false);

        this.firebaseAuth = FirebaseAuth.getInstance();
        this.currentUser = this.firebaseAuth.getCurrentUser();

        this.songs = new ArrayList<>();

        this.svSongsFilter = view.findViewById(R.id.svSongsFilter);
        this.rvSongsFilter = view.findViewById(R.id.rvSongsFilter);

        this.svSongsFilter.setOnQueryTextListener(this);
        getSongsFromFirebase();
        this.songsAdapter = new SongsAdapter(this.songs, view.getContext(), this);

        this.rvSongsFilter = view.findViewById(R.id.rvSongsFilter);
        this.rvSongsFilter.setHasFixedSize(true);
        this.rvSongsFilter.setLayoutManager(new LinearLayoutManager(getContext()));
        this.rvSongsFilter.setAdapter(this.songsAdapter);

        return view;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


    @Override
    public boolean onQueryTextChange(final String newText) {
        this.songsAdapter.filter(newText);
        return false;

    }

    @Override
    public void onSongClick(int pos) {
        Intent i = new Intent(getContext(), SongView.class);
        i.putExtra("playlistPosition", pos);
        i.putExtra("playlistId", this.playlistID);

        startActivity(i);
    }

    private void getSongsFromFirebase() {
        Query songsQuery = db.collection("Songs")
                .whereEqualTo("playlistId", this.playlistID)
                .orderBy("addedTimestamp")
                .orderBy("deezerTrackId");

        songsQuery.addSnapshotListener((documentSnapshots, e) -> {
            if (documentSnapshots == null) return;
            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                if (doc.getType() == DocumentChange.Type.ADDED) {
                    String deezerTrackId = String.valueOf(doc.getDocument().get("deezerTrackId"));
                    this.songsAdapter.addSong(new Song());
                    fetchSongMetadata(deezerTrackId, this.songsAdapter.getItemCount() - 1);
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
                        this.songsAdapter.setSong(new Song(songName, artistName, duration, coverUrl, deezerTrackId), pos);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.d("JSON", "onErrorResponse: " + error.getMessage()));
        this.queue.add(jsonObjectRequest);
    }
}