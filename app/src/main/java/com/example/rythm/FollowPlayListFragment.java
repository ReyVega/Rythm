package com.example.rythm;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class FollowPlayListFragment extends Fragment implements FollowPlayListAdapter.onSongListener {

    private RecyclerView rvFollowPlayList;
    private List<Song> followPlaylist;
    private FollowPlayListAdapter followPlayListAdapter;

    public FollowPlayListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_follow_play_list, container, false);

        this.followPlaylist = new ArrayList<>();
        this.followPlaylist.add(new Song());

        this.followPlayListAdapter = new FollowPlayListAdapter(this.followPlaylist, getContext(), this);

        this.rvFollowPlayList = view.findViewById(R.id.rvFollowPlayList);
        this.rvFollowPlayList.setHasFixedSize(true);
        this.rvFollowPlayList.setLayoutManager(new LinearLayoutManager(view.getContext()));
        this.rvFollowPlayList.setAdapter(this.followPlayListAdapter);
        return view;
    }

    @Override
    public void onSongClick(int pos) {

    }
}