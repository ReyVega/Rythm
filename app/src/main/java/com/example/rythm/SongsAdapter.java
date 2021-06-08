package com.example.rythm;

import android.content.Context;
import android.os.Build;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {
    private List<Song> songs,
            songsFiltered;
    private LayoutInflater inflater;
    private Context context;
    private onSongListener onSongListener;

    public SongsAdapter(List<Song> songs, Context context, onSongListener onSongListener) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.songs = songs;
        this.onSongListener = onSongListener;

        this.songsFiltered = new ArrayList<>();
        this.songsFiltered.addAll(songs);
    }

    @Override
    public int getItemCount() {
        return this.songs == null ? 0 : this.songs.size();
    }

    @Override
    public SongsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = this.inflater.inflate(R.layout.song_element, null);
        return new ViewHolder(v, this.onSongListener);
    }

    @Override
    public void onBindViewHolder(final SongsAdapter.ViewHolder holder, final int position) {
        holder.bindData(this.songs.get(position));
    }

    public void setPlaylist(List<Song> songs) {
        this.songs = songs;
    }

    public void addSong(Song song) {
        this.songs.add(song);
        this.songsFiltered.add(song);
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        NetworkImageView nivCover;
        TextView songName,
                artistName,
                duration;
        onSongListener onSongListener;

        ViewHolder(View itemView, onSongListener onSongListener) {
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
}
