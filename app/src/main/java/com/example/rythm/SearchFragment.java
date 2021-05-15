package com.example.rythm;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements AddSongAdapter.OnSongListener, AddSongAdapter.AddSongListener {

    private AddSongAdapter addSongAdapter;
    private List<Song> songs;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        this.songs = new ArrayList<>();

        this.songs.add(new Song("agus","gay", "popo","2323"));

        this.addSongAdapter = new AddSongAdapter(this.songs, view.getContext(), this, this);
        RecyclerView rv = view.findViewById(R.id.recyclerViewAddSongs);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(view.getContext()));
        rv.setAdapter(this.addSongAdapter);
        return view;
    }

    @Override
    public void onSongClick(int pos) {
    }

    @Override
    public void onBtnClick(int pos) {
    }
}