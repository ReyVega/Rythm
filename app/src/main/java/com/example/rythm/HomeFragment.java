package com.example.rythm;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final CollectionReference PlaylistsCollectionReference = db.collection("Playlists");

    private View view;

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
        this.view = inflater.inflate(R.layout.fragment_home, container, false);

        this.followPlayListFragment = new FollowPlayListFragment();

        this.recommendedSongs = new ArrayList<>();
        this.recommendedSongsAdapter = new RecommendedSongsAdapter(this.recommendedSongs, view.getContext(), this);
        this.rvRecSongs = view.findViewById(R.id.rvRecSongs);
        this.rvRecSongs.setHasFixedSize(true);
        this.rvRecSongs.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false));
        this.rvRecSongs.setAdapter(this.recommendedSongsAdapter);

        this.recommendedPlayLists = new ArrayList<>();
        this.recommendedPlayListsAdapter = new RecommendedPlayListsAdapter(this.recommendedPlayLists, view.getContext(), this);
        getTopPlaylists();

        return view;
    }

    @Override
    public void onRecSongClicked(int pos) {

    }

    @Override
    public void onRecPlayListClick(int pos) {
        Playlist playlist = this.recommendedPlayLists.get(pos);

        FragmentManager mr = getParentFragmentManager();
        assert mr != null;
        FragmentTransaction transaction = mr.beginTransaction();
        FollowPlayListFragment followPlaylistFragment = new FollowPlayListFragment(playlist.getName());
        followPlaylistFragment.setPlaylistId(playlist.getPlaylistId());
        followPlaylistFragment.setImagePlayList(playlist.getImageURL());

        transaction.replace(R.id.container, followPlaylistFragment, TAG_FRAGMENT);
        transaction.commit();
    }

    public void getTopPlaylists() {
        Query query = this.PlaylistsCollectionReference
                .orderBy("userId")
                .whereNotEqualTo("userId", this.user.getUid())
                .orderBy("followers", Query.Direction.DESCENDING)
                .limit(10);

        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = document.get("name").toString(),
                                       playlistID = document.getId(),
                                       imageURL = document.get("imageURL").toString();

                                Playlist playlist = new Playlist(name, playlistID);
                                playlist.setImageURL(imageURL);
                                recommendedPlayListsAdapter.addRecommendedPlaylist(playlist);
                                Log.d("Home Playlists", document.getId() + " => " + document.getData());
                            }

                            rvRecPlayLists = view.findViewById(R.id.rvRecPlayList);
                            rvRecPlayLists.setHasFixedSize(true);
                            rvRecPlayLists.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false));
                            rvRecPlayLists.setAdapter(recommendedPlayListsAdapter);
                        } else {
                            Log.d("Home Playlists", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}