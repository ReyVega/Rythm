package com.example.rythm;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SearchAddSongFragment extends Fragment implements AddSongAdapter.OnSongListener, AddSongAdapter.AddSongListener, SearchView.OnQueryTextListener {

    private AddSongAdapter addSongAdapter;
    private List<Song> songs;
    private SearchView svSearchSongFilter;

    private RequestQueue queue;

    //Connection to Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference songsCollectionReference = db.collection("Songs");

    private View view;

    private ImageButton ibCloseAddSong;

    private static final String TAG_FRAGMENT = "fragment";


    private RecyclerView rv;

    private String playlistId, playlistName;

    public SearchAddSongFragment() {
        // Required empty public constructor
    }

    public SearchAddSongFragment(String playlistId, String playlistName) {
        this.playlistId = playlistId;
        this.playlistName = playlistName;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search_add_song, container, false);

        this.songs = new ArrayList<>();

        this.svSearchSongFilter = view.findViewById(R.id.svPlaylistFilter);
        this.svSearchSongFilter.setOnQueryTextListener(this);

        this.ibCloseAddSong = view.findViewById(R.id.ibCloseAddSong);

        this.ibCloseAddSong.setOnClickListener(v -> {
            FragmentManager fr = getFragmentManager();
            assert fr != null;
            FragmentTransaction transaction = fr.beginTransaction();
            PlayListFragment playListFragment = new PlayListFragment(playlistName);
            playListFragment.setPlaylistId(playlistId);
            transaction.replace(R.id.container, playListFragment, TAG_FRAGMENT);
            transaction.commit();
        });

        this.addSongAdapter = new AddSongAdapter(this.songs, this.playlistId, view.getContext(), this, this);
        rv = view.findViewById(R.id.recyclerViewAddSongs);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(view.getContext()));
        rv.setAdapter(this.addSongAdapter);

        return view;
    }


    private void searchSongsInDeezer(String searchQuery) {
        this.addSongAdapter.clearSongs();

        if (searchQuery.length() == 0) return;
        if (!isAdded()) return; // Avoids exception

        this.queue = RequestController.getInstance(getContext()).getRequestQueue();

        Log.d("deezer", "searchSongsInDeezer: hola");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                getString(R.string.search_endpoint_deezer_api) + '"' +  searchQuery + '"', null,
                queryResults -> {
                    try {
                        if (!queryResults.has("data")) return;


                        JSONArray data = queryResults.getJSONArray("data");

                        Log.d("deezer", "searchSongsInDeezer: " + data.toString());

                        final int MAX_RESULTS = 10;

                        for (int i=0; i < Math.min(MAX_RESULTS, data.length()); i++) {
                            JSONObject track = data.getJSONObject(i);

                            String  deezerTrackId = track.getString("id"),
                                    songName = track.getString("title");
                            JSONObject artist = track.getJSONObject("artist");
                            String artistName = artist.getString("name");
                            int duration = track.getInt("duration");
                            JSONObject album = track.getJSONObject("album");
                            String coverUrl = album.getString("cover");

                            if (songName.length() > 0 && artistName.length() > 0 && duration != 0 && coverUrl.length() > 0 && deezerTrackId.length() > 0) {
                                this.addSongAdapter.addSong(new Song(songName, artistName, duration, coverUrl, deezerTrackId));
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.d("JSON", "onErrorResponse: " + error.getMessage()));

        queue.add(jsonObjectRequest);
    }

    @Override
    public void onSongClick(int pos) {
        Intent i = new Intent(getContext(), SongView.class);
        i.putExtra("deezerTrackId", this.songs.get(pos).getDeezerTrackId());
        i.putExtra("playlistName", "");
        startActivity(i);
    }

    private void addSongToPlaylistInFirestore(String deezerTrackId, String playlistId) {
        Map<String, String> songObj = new HashMap<>();
        songObj.put("deezerTrackId", deezerTrackId);
        songObj.put("playlistId", playlistId);

        songsCollectionReference.add(songObj)
                .addOnSuccessListener(documentReference -> documentReference.get()
                        .addOnCompleteListener(task1 -> {
                            if (Objects.requireNonNull(task1.getResult()).exists()) {
                                // encontrar cancion en youtube
                                Toast.makeText(getContext(), "Song added", Toast.LENGTH_LONG).show();
                            }
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Song can not be added", Toast.LENGTH_LONG).show();
                });
    }

    private void deleteSongInPlaylistFromFirestore(String deezerTrackId, String playlistId) {
        songsCollectionReference
                .whereEqualTo("playlistId", playlistId)
                .whereEqualTo("deezerTrackId", deezerTrackId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            songsCollectionReference.document(document.getId()).delete();
                        }

                    } else {
                        Log.d("error", "Error getting documents: ", task.getException());
                    }
                });
    }

    @Override
    public void onBtnClick(int pos, boolean isAdded) {
        if (isAdded) {
            deleteSongInPlaylistFromFirestore(this.songs.get(pos).getDeezerTrackId(), this.playlistId);
        }
        else {
            addSongToPlaylistInFirestore(this.songs.get(pos).getDeezerTrackId(), this.playlistId);
        }
    }


    private final int waitingTime = 200;
    private CountDownTimer cntr;

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String newText) {
        if (cntr != null) {
            cntr.cancel();
        }
        cntr = new CountDownTimer(waitingTime, 500) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                searchSongsInDeezer(newText);
            }
        };
        cntr.start();
        return false;

    }
}