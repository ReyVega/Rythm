package com.example.rythm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PlayListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Song> songs,
                       songsFiltered;
    private LayoutInflater inflater;
    private Context context;
    private onSongListener onSongListener;
    private final int HEADER = 1;
    private final int NORMAL = 2;
    private FragmentManager fm;
    private String playListName = "";
    private String playListID = "";
    private String imageURL = "";

    public PlayListAdapter(List<Song> songs, Context context, onSongListener onSongListener) {
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
            v = this.inflater.inflate(R.layout.header_playlist, null);
            return new HeaderViewHolder(v);
        } else {
            v = this.inflater.inflate(R.layout.song_element, null);
            return new SongsHolder(v, this.onSongListener);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
         if (holder instanceof HeaderViewHolder) {

         } else {
             ((SongsHolder) holder).bindData(this.songs.get(position));
         }
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

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG_FRAGMENT = "Fragment";
        private ImageView btnAddSong,
                btnFilterSongs,
                btnEditPlayList,
                ivPlayList;
        private TextView tvPlayListName;
        private SearchAddSongFragment searchAddSongFragment;
        private FilterSongsFragment filterSongsFragment;
        private EditPlayListFragment editPlayListFragment;

        HeaderViewHolder(View view) {
            super(view);
            this.searchAddSongFragment = new SearchAddSongFragment(playListID, playListName);
            this.filterSongsFragment = new FilterSongsFragment(playListID);
            this.editPlayListFragment = new EditPlayListFragment(playListID);

            this.btnAddSong = view.findViewById(R.id.btnAddSong);
            this.btnFilterSongs = view.findViewById(R.id.btnFilterSongs);
            this.btnEditPlayList = view.findViewById(R.id.btnEditPlaylist);
            this.tvPlayListName = view.findViewById(R.id.tvPlayListName);
            this.ivPlayList = view.findViewById(R.id.imagePlayList);
            this.tvPlayListName.setText(playListName);

            if (!imageURL.equals("")) {
                Picasso.with(context).load(imageURL).into(this.ivPlayList);
            } else {
                this.ivPlayList.setImageResource(R.drawable.playlist_default_photo);
            }

            this.btnAddSong.setOnClickListener(v -> {
                this.setFragment(this.searchAddSongFragment);
            });
            this.btnFilterSongs.setOnClickListener(v -> {
                this.setFragment(this.filterSongsFragment);
            });
            this.btnEditPlayList.setOnClickListener(v -> {
                this.setFragment(this.editPlayListFragment);
            });
        }

        public void setFragment(Fragment fragment) {
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.container, fragment, TAG_FRAGMENT);
            transaction.commit();
        }
    }

    public class SongsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        NetworkImageView nivCover;
        TextView songName,
                 artistName,
                 duration;
        onSongListener onSongListener;

        SongsHolder(View itemView, onSongListener onSongListener) {
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
}
