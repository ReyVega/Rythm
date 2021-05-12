package com.example.rythm;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment implements LibraryAdapter.onPlayListListener {

    private static final String TAG_FRAGMENT = "fragment";
    private List<Playlist> playlists;
    private FloatingActionButton addPlaylist;
    private LibraryAdapter libraryAdapter;
    private PlayListFragment playListFragment;

    public LibraryFragment() {
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

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Playlist name");
        builder.setMessage("");
        builder.setPositiveButton("OK",null);
        builder.create();
        builder.show();

        this.playListFragment = new PlayListFragment();

        this.playlists = new ArrayList<>();
        this.playlists.add(new Playlist("agus es gay"));

        this.libraryAdapter = new LibraryAdapter(this.playlists, view.getContext(), this);
        RecyclerView rv = view.findViewById(R.id.recyclerViewPlayLists);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(view.getContext()));
        rv.setAdapter(this.libraryAdapter);
        return view;
    }




    @Override
    public void onItemClick(int pos) {
        FragmentManager mr = getFragmentManager();
        FragmentTransaction transaction = mr.beginTransaction();
        transaction.replace(R.id.container, this.playListFragment, TAG_FRAGMENT);
        transaction.commit();
    }
}