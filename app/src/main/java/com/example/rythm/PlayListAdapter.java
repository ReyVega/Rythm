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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView iconImage;
        TextView songName,
                 artistName,
                 genreName;
        onSongListener onSongListener;

        ViewHolder(View itemView, onSongListener onSongListener) {
            super(itemView);
            this.iconImage = itemView.findViewById(R.id.iconImageView);
            this.songName = itemView.findViewById(R.id.songName);
            this.artistName = itemView.findViewById(R.id.artistName);
            this.genreName = itemView.findViewById(R.id.genreName);
            this.onSongListener = onSongListener;
            itemView.setOnClickListener(this);
        }

        void bindData(final Song item) {
            this.iconImage.setColorFilter(Color.parseColor("#0000FF"), PorterDuff.Mode.SRC_IN);
            this.songName.setText(item.getSongName());
            this.artistName.setText(item.getArtistName());
            this.genreName.setText(item.getGenreName());
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
