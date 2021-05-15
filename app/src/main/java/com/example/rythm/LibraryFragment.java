package com.example.rythm;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment implements LibraryAdapter.onPlayListListener {

    private static final String TAG_FRAGMENT = "fragment";
    private List<Playlist> playlists;
    private ImageView btnAddPlaylist,
                      btnFilterPlayLists;
    private LibraryAdapter libraryAdapter;
    private PlayListFragment playListFragment;
    private Button btnPlayListNameAlert;
    private EditText editPlayListNameAlert;

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

        LayoutInflater layoutInflater = LayoutInflater.from(view.getContext());
        View promptView = layoutInflater.inflate(R.layout.custom_dialog, null);

        final AlertDialog alertD = new AlertDialog.Builder(view.getContext()).create();

        this.btnAddPlaylist = view.findViewById(R.id.btnAddPlayList);
        this.btnFilterPlayLists = view.findViewById(R.id.btnFilterPlayLists);
        this.btnPlayListNameAlert = promptView.findViewById(R.id.btnPlayListNameAlert);
        this.editPlayListNameAlert = promptView.findViewById(R.id.editPlayListNameAlert);
        this.playlists = new ArrayList<>();

        alertD.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertD.setView(promptView);
        
        this.btnAddPlaylist.setOnClickListener(v -> {
            alertD.show();
        });

        this.btnPlayListNameAlert.setOnClickListener(v -> {
            if (editPlayListNameAlert.getText().toString().trim().isEmpty()) {
                editPlayListNameAlert.setError("Empty field");
            } else {
                this.libraryAdapter.addPlayList(this.editPlayListNameAlert.getText().toString());
                alertD.dismiss();
                sendToPlayList(this.editPlayListNameAlert.getText().toString());
            }
        });

        this.btnFilterPlayLists.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), FilterPlayListsView.class);
            startActivity(i);
        });

        this.libraryAdapter = new LibraryAdapter(this.playlists, view.getContext(), this);
        RecyclerView rv = view.findViewById(R.id.recyclerViewPlayLists);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(view.getContext()));
        rv.setAdapter(this.libraryAdapter);
        return view;
    }

    void sendToPlayList(String name) {
        this.playListFragment = new PlayListFragment(name);
        FragmentManager mr = getFragmentManager();
        FragmentTransaction transaction = mr.beginTransaction();
        transaction.replace(R.id.container, this.playListFragment, TAG_FRAGMENT);
        transaction.commit();
    }
    @Override
    public void onItemClick(int pos) {
        sendToPlayList(this.playlists.get(pos).getName());
    }
}