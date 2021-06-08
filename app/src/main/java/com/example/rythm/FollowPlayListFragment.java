package com.example.rythm;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FollowPlayListFragment extends Fragment implements FollowPlayListAdapter.onSongListener {

    private FollowPlayListAdapter playListAdapter;

    private static final String TAG_FRAGMENT = "fragment";
    private List<Song> songs;
    private String playListName,
            imageURL,
            author;
    private RecyclerView recyclcerViewSongs;

    private RequestQueue queue;

    //Connection to Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference songsCollectionReference = db.collection("Songs"),
                                playlistsCollectionReference = db.collection("Playlists");


    private String playlistId;

    public FollowPlayListFragment() {
        // Required empty public constructor
    }

    public FollowPlayListFragment(String playListName) {
        this.playListName = playListName;
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
        View view = inflater.inflate(R.layout.fragment_follow_play_list, container, false);

        this.songs = new ArrayList<>();
        this.playListAdapter = new FollowPlayListAdapter(this.songs, view.getContext(), this);
        this.gePlaylisttAuthor(playlistId);

        this.playListAdapter = new FollowPlayListAdapter(this.songs, getContext(), this);
        this.playListAdapter.setFm(getParentFragmentManager());
        this.playListAdapter.setPlayListName(this.playListName);
        this.playListAdapter.setPlayListID(this.playlistId);
        this.playListAdapter.setPlayListImage(this.imageURL);

        this.recyclcerViewSongs = view.findViewById(R.id.rvFollowPlayList);
        this.recyclcerViewSongs.setHasFixedSize(true);
        this.recyclcerViewSongs.setLayoutManager(new LinearLayoutManager(view.getContext()));
        this.recyclcerViewSongs.setAdapter(this.playListAdapter);
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

    private void gePlaylisttAuthor(String playlistId) {
        DocumentReference documentReference = playlistsCollectionReference.document(playlistId);

        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document == null) return;
                if (document.exists()) {
                    String authorId = document.getString("userId");

//                    try {
//                        UserRecord userRecord = FirebaseAuth.getInstance().getUser(authorId);
//                        this.author = userRecord.getDisplayName();
//                        getSongsFromFirebase();
//                    } catch (FirebaseAuthException e) {
//                        Toast.makeText(getContext(), "Playlist not found", Toast.LENGTH_LONG).show();
//                        e.printStackTrace();
//                    }


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