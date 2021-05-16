package com.example.rythm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

public class FilterPlayListsView extends AppCompatActivity implements LibraryAdapter.onPlayListListener, SearchView.OnQueryTextListener {

    private List<Playlist> playlists;
    private SearchView svPlayListsFilter;
    private RecyclerView rvPlayListsFilter;
    private LibraryAdapter playListsFilterAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_play_lists_view);

        this.svPlayListsFilter = findViewById(R.id.svPlayListsFilter);
        this.rvPlayListsFilter = findViewById(R.id.rvPlayListsFilter);
        this.playlists = new ArrayList<>();
        this.playlists.add(new Playlist("popo"));
        this.playlists.add(new Playlist("pipi"));
        this.playlists.add(new Playlist("kaka"));



        this.svPlayListsFilter.setOnQueryTextListener(this);
        this.playListsFilterAdapter = new LibraryAdapter(this.playlists, this, this);
        this.rvPlayListsFilter = findViewById(R.id.rvPlayListsFilter);
        this.rvPlayListsFilter.setHasFixedSize(true);
        this.rvPlayListsFilter.setLayoutManager(new LinearLayoutManager(this));
        this.rvPlayListsFilter.setAdapter(this.playListsFilterAdapter);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        this.playListsFilterAdapter.filter(newText);
        return false;
    }

    @Override
    public void onItemClick(int pos) {

    }
}