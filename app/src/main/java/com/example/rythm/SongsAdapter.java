package com.example.rythm;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {
    private List<Song> songs;
    private LayoutInflater inflater;
    private Context context;

    public SongsAdapter(List<Song> songs, Context context) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.songs = songs;
    }

    @Override
    public int getItemCount() {
        return this.songs == null ? 0 : this.songs.size();
    }

    @Override
    public SongsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = this.inflater.inflate(R.layout.song_element, null);
        return new SongsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final SongsAdapter.ViewHolder holder, final int position) {
        holder.bindData(this.songs.get(position));
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImage;
        TextView songName,
                 artistName,
                 genreName;
        ViewHolder(View itemView) {
            super(itemView);
            this.iconImage = itemView.findViewById(R.id.iconImageView);
            this.songName = itemView.findViewById(R.id.songName);
            this.artistName = itemView.findViewById(R.id.artistName);
            this.genreName = itemView.findViewById(R.id.genreName);
        }

        void bindData(final Song item) {
            this.iconImage.setColorFilter(Color.parseColor("#0000FF"), PorterDuff.Mode.SRC_IN);
            this.songName.setText(item.getSongName());
            this.artistName.setText(item.getArtistName());
            this.genreName.setText(item.getGenreName());
        }
    }
}
