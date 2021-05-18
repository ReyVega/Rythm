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

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

public class AddSongAdapter  extends RecyclerView.Adapter<AddSongAdapter.ViewHolder>  {
    private List<Song> songs;
    private LayoutInflater inflater;
    private Context context;
    private OnSongListener onSongListener;
    private AddSongListener addSongListener;


    public AddSongAdapter(List<Song> songs, Context context, OnSongListener onSongListener, AddSongListener addSongListener) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.songs = songs;
        this.onSongListener = onSongListener;
        this.addSongListener = addSongListener;
    }

    @Override
    public int getItemCount() {
        return this.songs == null ? 0 : this.songs.size();
    }

    @Override
    public AddSongAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = this.inflater.inflate(R.layout.add_song_element, null);
        return new AddSongAdapter.ViewHolder(v, this.onSongListener, this.addSongListener);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.bindData(this.songs.get(position));
    }

    public void addSong(Song song) {
        this.songs.add(song);
        notifyDataSetChanged();
    }

    public void setSongs(List<Song> newSongs) {
        this.songs = newSongs;
        notifyDataSetChanged();
    }

    public void clearSongs() {
        this.songs.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        NetworkImageView nivAddSongCover;
        TextView songName,
                artistName;
        OnSongListener onSongListener;
        AddSongListener addSongListener;
        ImageView btnAdd;

        ViewHolder(View itemView, OnSongListener onSongListener, AddSongListener addSongListener) {
            super(itemView);
            this.nivAddSongCover = itemView.findViewById(R.id.nivAddSongCover);
            this.songName = itemView.findViewById(R.id.songName);
            this.artistName = itemView.findViewById(R.id.artistName);
            this.btnAdd = itemView.findViewById(R.id.btnAdd);
            this.onSongListener = onSongListener;
            this.addSongListener = addSongListener;

            this.btnAdd.setOnClickListener(v -> {
                this.onClickBtn(v);
                this.btnAdd.setImageResource(R.drawable.ic_check);
            });

            itemView.setOnClickListener(this);
        }
        private void loadCover(String coverUrl){
            this.nivAddSongCover.setDefaultImageResId(R.drawable.exo_ic_default_album_image);
            this.nivAddSongCover.setErrorImageResId(R.drawable.exo_ic_default_album_image);
            ImageLoader imageLoader = RequestController.getInstance(context).getImageLoader();

            imageLoader.get(coverUrl, ImageLoader.getImageListener(nivAddSongCover,
                    R.drawable.exo_ic_default_album_image, android.R.drawable
                            .ic_dialog_alert));
            nivAddSongCover.setImageUrl(coverUrl, imageLoader);
        }


        void bindData(final Song song) {
            loadCover(song.getCoverUrl());
            this.songName.setText(song.getSongName());
            this.artistName.setText(song.getArtistName());
        }

        @Override
        public void onClick(View v) {
            this.onSongListener.onSongClick(getAdapterPosition());
        }

        public void onClickBtn(View v) {
            this.addSongListener.onBtnClick(getAdapterPosition());
        }
    }
    public interface OnSongListener {
        void onSongClick(int pos);
    }

    public interface AddSongListener {
        void onBtnClick(int pos);
    }
}
