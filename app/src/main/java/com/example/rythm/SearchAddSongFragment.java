package com.example.rythm;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchAddSongFragment extends Fragment implements AddSongAdapter.OnSongListener, AddSongAdapter.AddSongListener, SearchView.OnQueryTextListener {

    private AddSongAdapter addSongAdapter;
    private List<Song> songs;
    private SearchView svSearchSongFilter;

    private RequestQueue queue;

    //Connection to Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private View view;

    private RecyclerView rv;

    public SearchAddSongFragment() {
        // Required empty public constructor
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

        this.addSongAdapter = new AddSongAdapter(this.songs, view.getContext(), this, this);
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
    }

    @Override
    public void onBtnClick(int pos) {

    }


    private int waitingTime = 200;
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