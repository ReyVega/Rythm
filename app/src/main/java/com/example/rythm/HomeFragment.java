package com.example.rythm;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        this.recommendedSongs.add(new Song("hola","",3,"",""));
        this.recommendedSongsAdapter = new RecommendedSongsAdapter(this.recommendedSongs, view.getContext(), this);
        this.rvRecSongs = view.findViewById(R.id.rvRecSongs);
        this.rvRecSongs.setHasFixedSize(true);
        this.rvRecSongs.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false));
        this.rvRecSongs.setAdapter(this.recommendedSongsAdapter);

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

    }

    @Override
    public void onRecPlayListClick(int pos) {
        FragmentManager fm = getParentFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.container, this.followPlayListFragment, TAG_FRAGMENT);
        transaction.commit();
    }
}