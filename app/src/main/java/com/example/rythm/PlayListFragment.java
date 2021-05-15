package com.example.rythm;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

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

        this.songs = new ArrayList<>();
        this.songs.add(new Song("agus", "es", "gay", "123"));

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
}