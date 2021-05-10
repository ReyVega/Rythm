package com.example.rythm;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class PlayListFragment extends Fragment implements PlayListAdapter.onSongListener {

    private List<Song> songs;
    private FloatingActionButton addSong;
    private PlayListAdapter playListAdapter;

    public PlayListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        this.songs = new ArrayList<>();
        this.songs.add(new Song("agus", "es", "gay", "123"));

        this.playListAdapter = new PlayListAdapter(this.songs, view.getContext(), this);
        RecyclerView rv = view.findViewById(R.id.recyclerViewPlayLists);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(view.getContext()));
        rv.setAdapter(this.playListAdapter);
        return view;
    }

    @Override
    public void onSongClick(int pos) {

    }
}