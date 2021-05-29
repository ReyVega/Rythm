package com.example.rythm;

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

public class FilterSongsFragment extends Fragment implements PlayListAdapter.onSongListener, SearchView.OnQueryTextListener {

    private static final String TAG_FRAGMENT = "fragment";
    private List<Song> songs;
    private SearchView svSongsFilter;
    private RecyclerView rvSongsFilter;
    private PlayListAdapter songsAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private final int waitingTime = 200;
    private CountDownTimer cntr;

    public FilterSongsFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_songs_view, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        this.songs = new ArrayList<>();

        this.svSongsFilter = view.findViewById(R.id.svSongsFilter);
        this.rvSongsFilter = view.findViewById(R.id.rvSongsFilter);

        this.svSongsFilter.setOnQueryTextListener(this);
        this.songsAdapter = new PlayListAdapter(this.songs, view.getContext(), this);
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
        if (cntr != null) {
            cntr.cancel();
        }
        cntr = new CountDownTimer(waitingTime, 500) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                songsAdapter.filter(newText);
            }
        };
        cntr.start();
        return false;

    }

    @Override
    public void onSongClick(int pos) {

    }
}