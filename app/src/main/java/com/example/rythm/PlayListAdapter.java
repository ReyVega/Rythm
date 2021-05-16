package com.example.rythm;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.ViewHolder>  {
    private List<Song> songs;
    private LayoutInflater inflater;
    private Context context;
    private onSongListener onSongListener;

    public PlayListAdapter(List<Song> songs, Context context, onSongListener onSongListener) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.songs = songs;
        this.onSongListener = onSongListener;
    }

    @Override
    public int getItemCount() {
        return this.songs == null ? 0 : this.songs.size();
    }

    @Override
    public PlayListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = this.inflater.inflate(R.layout.song_element, null);
        return new PlayListAdapter.ViewHolder(v, this.onSongListener);
    }

    @Override
    public void onBindViewHolder(final PlayListAdapter.ViewHolder holder, final int position) {
        holder.bindData(this.songs.get(position));
    }

    public void setPlaylist(List<Song> songs) {
        this.songs = songs;
    }

    public void addSong(Song song) {
        this.songs.add(song);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        NetworkImageView nivCover;
        TextView songName,
                 artistName,
                 genreName;
        onSongListener onSongListener;

        ViewHolder(View itemView, onSongListener onSongListener) {
            super(itemView);
            this.nivCover = itemView.findViewById(R.id.nivCover);
            this.songName = itemView.findViewById(R.id.songName);
            this.artistName = itemView.findViewById(R.id.artistName);
            this.genreName = itemView.findViewById(R.id.duration);
            this.onSongListener = onSongListener;
            itemView.setOnClickListener(this);
        }

        private void loadCover(String coverUrl){
            this.nivCover.setDefaultImageResId(R.drawable.exo_ic_default_album_image);
            this.nivCover.setErrorImageResId(R.drawable.exo_ic_default_album_image);
            Log.d("CACA", "loadCover: siuuuu");
            ImageLoader imageLoader = RequestController.getInstance(context).getImageLoader();

            imageLoader.get(coverUrl, ImageLoader.getImageListener(nivCover,
                    R.drawable.exo_ic_default_album_image, android.R.drawable
                            .ic_dialog_alert));
            nivCover.setImageUrl(coverUrl, imageLoader);
        }


        void bindData(final Song song) {
            loadCover(song.getCoverUrl());
            this.songName.setText(song.getSongName());
            this.artistName.setText(song.getArtistName());
            this.genreName.setText(song.getFormattedDuration());
        }

        @Override
        public void onClick(View v) {
            this.onSongListener.onSongClick(getAdapterPosition());
        }
    }
    public interface onSongListener {
        void onSongClick(int pos);
    }
}
