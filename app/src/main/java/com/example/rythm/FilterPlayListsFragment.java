package com.example.rythm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FilterPlayListsFragment extends Fragment implements LibraryAdapter.onPlayListListener, SearchView.OnQueryTextListener {

    private static final String TAG_FRAGMENT = "fragment";
    private List<Playlist> playlists;
    private SearchView svPlayListsFilter;
    private RecyclerView rvPlayListsFilter;
    private LibraryAdapter playListsFilterAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private final int waitingTime = 200;
    private CountDownTimer cntr;

    public FilterPlayListsFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_filter_play_lists_view, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        this.playlists = new ArrayList<>();

        getPlaylistsFromFirebase(currentUser.getUid());

        this.svPlayListsFilter = view.findViewById(R.id.svPlaylistFilter);
        this.rvPlayListsFilter = view.findViewById(R.id.rvPlayListsFilter);

        this.svPlayListsFilter.setOnQueryTextListener(this);
        this.playListsFilterAdapter = new LibraryAdapter(this.playlists, getContext(), this);
        this.rvPlayListsFilter = view.findViewById(R.id.rvPlayListsFilter);
        this.rvPlayListsFilter.setHasFixedSize(true);
        this.rvPlayListsFilter.setLayoutManager(new LinearLayoutManager(getContext()));
        this.rvPlayListsFilter.setAdapter(this.playListsFilterAdapter);

        return view;
    }

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
                playListsFilterAdapter.filter(newText);
            }
        };
        cntr.start();
        return false;

    }

    void redirectToPlayListFragment(int pos) {
        PlayListFragment playListFragment = new PlayListFragment(this.playlists.get(pos).getName());
        playListFragment.setPlaylistId(playlists.get(pos).getPlaylistId());
        FragmentManager mr = getFragmentManager();
        assert mr != null;
        FragmentTransaction transaction = mr.beginTransaction();
        transaction.replace(R.id.container, playListFragment, TAG_FRAGMENT);

        transaction.commit();
    }



    @Override
    public void onItemClick(int pos) {
        redirectToPlayListFragment(pos);
    }




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
                        this.playListsFilterAdapter.addPlayList(new Playlist(name, playlistId));
                    }
                }
            }
        });
    }
}