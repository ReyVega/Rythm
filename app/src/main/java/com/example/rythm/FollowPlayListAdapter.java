package com.example.rythm;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FollowPlayListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<Song> songs,
            songsFiltered;
    private LayoutInflater inflater;
    private Context context;
    private FollowPlayListAdapter.onSongListener onSongListener;
    private final int HEADER = 1;
    private final int NORMAL = 2;
    private FragmentManager fm;
    private String playListName = "";
    private String playListID = "";
    private String imageURL = "";
    private String author = "";
    private boolean isFollowed = false;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private final CollectionReference PlaylistsCollectionReference = db.collection("Playlists"),
                                      FollowedPlaylistsCollectionReference = db.collection("FollowedPlaylists");
    ;

    public FollowPlayListAdapter(List<Song> songs, Context context, FollowPlayListAdapter.onSongListener onSongListener) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.songs = songs;
        this.onSongListener = onSongListener;

        this.songsFiltered = new ArrayList<>();
        this.songsFiltered.addAll(this.songs);
    }

    @Override
    public int getItemCount() {
        return this.songs == null ? 0 : this.songs.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;

        if (viewType == HEADER) {
            v = this.inflater.inflate(R.layout.header_follow_element, null);
            return new FollowPlayListAdapter.HeaderViewHolder(v);
        } else {
            v = this.inflater.inflate(R.layout.song_element, null);
            return new FollowPlayListAdapter.SongsHolder(v, this.onSongListener);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof FollowPlayListAdapter.HeaderViewHolder) {

        } else {
            ((FollowPlayListAdapter.SongsHolder) holder).bindData(this.songs.get(position));
        }
    }

    public void filter(final String filteredSearch) {
        String query = filteredSearch.toLowerCase();

        this.songs.clear();
        if (query.isEmpty()) {
            this.songs.addAll(this.songsFiltered);
        } else {
            ArrayList<Song> newlist = new ArrayList<>();
            for (Song song: this.songsFiltered) {
                if (song.getSongName().toLowerCase().contains(query)) {
                    newlist.add(song);
                }
            }
            if (newlist.size() > 0) {
                this.songs.addAll(newlist);
            }
        }
        notifyDataSetChanged();
    }

    public void setPlaylist(List<Song> songs) {
        this.songs = songs;
    }

    public void addSong(Song song) {
        this.songs.add(song);
        notifyDataSetChanged();
    }

    public void setSong(Song song, int pos) {
        this.songs.get(pos).setSong(song);
        notifyDataSetChanged();
    }

    public void clearSongs() {
        this.songs.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEADER;
        } else {
            return NORMAL;
        }
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG_FRAGMENT = "Fragment";
        private ImageView btnFilterSongs,
                          ivPlayList;
        private TextView tvPlayListName,
                         tvAuthorFollow;
        private FilterSongsFragment filterSongsFragment;
        private Button btnAddPlayListFollow;


        HeaderViewHolder(View view) {
            super(view);
            getIsFollowedFromFirestore(playListID);

            this.filterSongsFragment = new FilterSongsFragment(playListID);
            this.btnFilterSongs = view.findViewById(R.id.btnFilterSongsFollow);
            this.btnAddPlayListFollow= view.findViewById(R.id.btnAddPlayListFollow);
            this.tvPlayListName = view.findViewById(R.id.tvPlayListNameFollow);
            this.tvAuthorFollow = view.findViewById(R.id.tvAuthorFollow);
            this.ivPlayList = view.findViewById(R.id.imagePlayListFollow);

            if (playListName != null) this.tvPlayListName.setText(playListName);
            if (author != null) this.tvAuthorFollow.setText(author);

            if (!imageURL.equals("")) {
                Picasso.with(context).load(imageURL).into(this.ivPlayList);
            } else {
                this.ivPlayList.setImageResource(R.drawable.song_default_photo);
            }

            this.btnAddPlayListFollow.setOnClickListener(v -> {
                if (isFollowed) {
                    isFollowed = false;
                    this.btnAddPlayListFollow.setText("Follow");
                    unfollowPlaylist();
                    updateFollowers(playListID, false);
                } else {
                    isFollowed = true;
                    this.btnAddPlayListFollow.setText("Unfollow");
                    followPlaylist();
                    updateFollowers(playListID, true);
                }
            });

            this.btnFilterSongs.setOnClickListener(v -> {
                this.setFragment(this.filterSongsFragment);
            });
        }

        public void setFragment(Fragment fragment) {
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.container, fragment, TAG_FRAGMENT);
            transaction.commit();
        }

        public void followPlaylist() {
            Map<String, Object> playlist = new HashMap<>();
            playlist.put("lastModified", FieldValue.serverTimestamp());
            playlist.put("playlistId", playListID);
            playlist.put("userId", user.getUid());

            db.collection("FollowedPlaylists").add(playlist)
                    .addOnSuccessListener(documentReference -> documentReference.get()
                            .addOnCompleteListener(task1 -> {
                                if (Objects.requireNonNull(task1.getResult()).exists()) {
                                    Toast.makeText(context, "Playlist followed", Toast.LENGTH_LONG).show();
                                }
                            }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Playlist can not be added", Toast.LENGTH_LONG).show();
                    });
        }

        public void unfollowPlaylist() {
            Query query = FollowedPlaylistsCollectionReference
                    .whereEqualTo("playlistId", playListID)
                    .whereEqualTo("userId", user.getUid());

            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        FollowedPlaylistsCollectionReference.document(document.getId()).delete();
                        Toast.makeText(context, "Playlist unfollowed", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d("Unfollow playlist", "Error getting documents: ", task.getException());
                }
            });
        }

        public void updateFollowers(String playlistID, boolean increases) {
            DocumentReference documentReference = PlaylistsCollectionReference.
                    document(playlistID);

            Map<String, Object> data = new HashMap<>();

            documentReference.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document == null) return;
                    if (document.exists()) {
                        try {
                            long followers = (long) document.get("followers");
                            data.put("followers", followers + (increases ? 1 : -1));
                        } catch (NullPointerException e) {
                            data.put("followers", increases ? 1 : 0);
                        }
                        documentReference.set(data, SetOptions.merge());
                    }
                }
            });
        }

        private void getIsFollowedFromFirestore(String playlistID) {
            Query query = FollowedPlaylistsCollectionReference
                    .whereEqualTo("playlistId", playlistID)
                    .whereEqualTo("userId", user.getUid());

            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        isFollowed = true;
                        this.btnAddPlayListFollow.setText("Unfollow");
                    } else {
                        isFollowed = false;
                        this.btnAddPlayListFollow.setText("Follow");
                    }
                }
            });
        }
    }

    public class SongsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        NetworkImageView nivCover;
        TextView songName,
                artistName,
                duration;
        FollowPlayListAdapter.onSongListener onSongListener;

        SongsHolder(View itemView, FollowPlayListAdapter.onSongListener onSongListener) {
            super(itemView);
            this.nivCover = itemView.findViewById(R.id.nivCover);
            this.songName = itemView.findViewById(R.id.songName);
            this.artistName = itemView.findViewById(R.id.artistName);
            this.duration = itemView.findViewById(R.id.duration);
            this.onSongListener = onSongListener;
            itemView.setOnClickListener(this);
        }

        private void loadCover(String coverUrl){
            this.nivCover.setDefaultImageResId(R.drawable.exo_ic_default_album_image);
            this.nivCover.setErrorImageResId(R.drawable.exo_ic_default_album_image);

            if (coverUrl==null || coverUrl.isEmpty()) {
                return;
            }

            ImageLoader imageLoader = RequestController.getInstance(context).getImageLoader();

            imageLoader.get(coverUrl, ImageLoader.getImageListener(nivCover,
                    R.drawable.exo_ic_default_album_image, android.R.drawable
                            .ic_dialog_alert));
            nivCover.setImageUrl(coverUrl, imageLoader);
        }

        public void bindData(final Song song) {
            loadCover(song.getCoverUrl());
            this.songName.setText(song.getSongName());
            this.artistName.setText(song.getArtistName());
            this.duration.setText(song.getFormattedDuration());
        }

        @Override
        public void onClick(View v) {
            this.onSongListener.onSongClick(getBindingAdapterPosition());
        }
    }
    public interface onSongListener {
        void onSongClick(int pos);
    }
    public void setFm(FragmentManager fm) {
        this.fm = fm;
    }

    public void setPlayListName(String playListName) {
        this.playListName = playListName;
    }

    public void setPlayListID(String playListID) {
        this.playListID = playListID;
    }

    public void setPlayListImage(String imageURL) { this.imageURL = imageURL; }

    public void setFollowed(boolean followed) {
        this.isFollowed = followed;
    }
}
