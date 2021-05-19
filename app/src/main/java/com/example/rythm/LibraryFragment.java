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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LibraryFragment extends Fragment implements LibraryAdapter.onPlayListListener {

    private static final String TAG_FRAGMENT = "fragment";
    private List<Playlist> playlists;
    private ImageView btnAddPlaylist,
                      btnFilterPlayLists;
    private LibraryAdapter libraryAdapter;
    private PlayListFragment playListFragment;
    private Button btnPlayListNameAlert;
    private EditText editPlayListNameAlert;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference playlistsCollectionReference = db.collection("Playlists");


    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

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

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        assert currentUser != null;
        getPlaylistsFromFirebase(currentUser.getUid());

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
                alertD.dismiss();
                String playlistName = this.editPlayListNameAlert.getText().toString().trim();
                createPlaylistInFirestore(currentUser.getUid(), playlistName);
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

    private void createPlaylistInFirestore(String userId, String name) {
        Map<String, String> playlistObj = new HashMap<>();
        playlistObj.put("userId", userId);
        playlistObj.put("name", name);

        playlistsCollectionReference.add(playlistObj)
                .addOnSuccessListener(documentReference -> documentReference.get()
                        .addOnCompleteListener(task1 -> {
                            if (Objects.requireNonNull(task1.getResult()).exists()) {
                                String playlistId = task1.getResult().getId();
                                redirectToPlayListFragment(name, playlistId);

                                Toast.makeText(getContext(), "Playlist created successfully", Toast.LENGTH_LONG).show();
                            }
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Playlist can not be created", Toast.LENGTH_LONG).show();
                });
    }

    void redirectToPlayListFragment(int pos) {
        this.playListFragment = new PlayListFragment(this.playlists.get(pos).getName());
        this.playListFragment.setPlaylistId(playlists.get(pos).getPlaylistId());
        FragmentManager mr = getFragmentManager();
        assert mr != null;
        FragmentTransaction transaction = mr.beginTransaction();
        transaction.replace(R.id.container, this.playListFragment, TAG_FRAGMENT);

        transaction.commit();
    }

    void redirectToPlayListFragment(String name, String playlistId) {
        this.playListFragment = new PlayListFragment(name);
        this.playListFragment.setPlaylistId(playlistId);
        FragmentManager mr = getFragmentManager();
        assert mr != null;
        FragmentTransaction transaction = mr.beginTransaction();
        transaction.replace(R.id.container, this.playListFragment, TAG_FRAGMENT);

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
                        this.libraryAdapter.addPlayList(new Playlist(name, playlistId));
                    }
                }
            }
        });
    }
}