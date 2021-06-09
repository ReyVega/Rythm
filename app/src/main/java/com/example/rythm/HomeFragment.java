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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment implements RecommendedSongsAdapter.onSongsListener, RecommendedPlayListsAdapter.onPlayListListener {

    private static final String TAG_FRAGMENT = "fragment";
    private RecommendedSongsAdapter recommendedSongsAdapter;
    private RecommendedPlayListsAdapter recommendedPlayListsAdapter;
    private List<Song> recommendedSongs;
    private List<Playlist> recommendedPlayLists;
    private RecyclerView rvRecSongs;
    private RecyclerView rvRecPlayLists;
    private FollowPlayListFragment followPlayListFragment;


    private RequestQueue queue;
    //Connection to Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference songsCollectionReference = db.collection("Songs");


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        this.followPlayListFragment = new FollowPlayListFragment();

        this.recommendedSongs = new ArrayList<>();
        this.recommendedSongsAdapter = new RecommendedSongsAdapter(this.recommendedSongs, view.getContext(), this);
        this.rvRecSongs = view.findViewById(R.id.rvRecSongs);
        this.rvRecSongs.setHasFixedSize(true);
        this.rvRecSongs.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false));
        this.rvRecSongs.setAdapter(this.recommendedSongsAdapter);

        getRecommendedSongsFromFirebase();

        this.recommendedPlayLists = new ArrayList<>();
        this.recommendedPlayLists.add(new Playlist("hola","dfijfio", false));
        this.recommendedPlayListsAdapter = new RecommendedPlayListsAdapter(this.recommendedPlayLists, view.getContext(), this);
        this.rvRecPlayLists = view.findViewById(R.id.rvRecPlayList);
        this.rvRecPlayLists.setHasFixedSize(true);
        this.rvRecPlayLists.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false));
        this.rvRecPlayLists.setAdapter(this.recommendedPlayListsAdapter);

        return view;
    }

    @Override
    public void onRecSongClicked(int pos) {
        Song clickedSong = recommendedSongsAdapter.getSong(pos);

        if (clickedSong != null) {
            Intent i = new Intent(getContext(), SongView.class);
            i.putExtra("selectedDeezerTrackId", clickedSong.getDeezerTrackId());
            i.putExtra("playlistName", "");
            startActivity(i);
        }
    }

    @Override
    public void onRecPlayListClick(int pos) {
        FragmentManager fm = getParentFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.container, this.followPlayListFragment, TAG_FRAGMENT);
        transaction.commit();
    }

    private void getRecommendedSongsFromFirebase() {
        if (!isAdded()) return;
        Query songsQuery = db.collection("SongsReproductions")
                .orderBy("reproductions", Query.Direction.DESCENDING)
                .limit(10);

        songsQuery.addSnapshotListener((documentSnapshots, e) -> {
            if (documentSnapshots == null) return;
            for (DocumentChange doc: documentSnapshots.getDocumentChanges()){
                if (doc.getType() == DocumentChange.Type.ADDED){
                    String deezerTrackId = doc.getDocument().getId();
                    recommendedSongsAdapter.addSong(new Song());
                    fetchSongMetadata(deezerTrackId, recommendedSongsAdapter.getItemCount() - 1);
                }
            }
        });
    }

    private void fetchSongMetadata(String deezerTrackId, int pos) {
        if (!isAdded()) return;
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
                        recommendedSongsAdapter.setSong(new Song(songName, artistName, duration, coverUrl, deezerTrackId), pos);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.d("JSON", "onErrorResponse: " + error.getMessage()));

        queue.add(jsonObjectRequest);
    }

}