package com.example.rythm;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class RecommendedSongsAdapter extends RecyclerView.Adapter<RecommendedSongsAdapter.ViewHolder> {

    private static final String TAG = "TAMALES";
    private List<Song> songs;
    private LayoutInflater inflater;
    private Context context;
    private onSongsListener onSongListener;


    public RecommendedSongsAdapter(List<Song> songs, Context context, onSongsListener onSongListener) {
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
    public RecommendedSongsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = this.inflater.inflate(R.layout.recommended_song, null);
        return new RecommendedSongsAdapter.ViewHolder(v, this.onSongListener);
    }

    @Override
    public void onBindViewHolder(final RecommendedSongsAdapter.ViewHolder holder, final int position) {
        holder.bindData(this.songs.get(position));
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

    public Song getSong(int pos) {
        if (pos >= 0 && pos < getItemCount()) {
            return songs.get(pos);
        }
        return null;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        NetworkImageView nivRecommendedCover;
        TextView tvRecSongName;
        onSongsListener onSongListener;

        ViewHolder(View itemView, onSongsListener onSongListener) {
            super(itemView);
            this.nivRecommendedCover = itemView.findViewById(R.id.nivRecommendedCover);
            this.tvRecSongName = itemView.findViewById(R.id.tvRecSong);
            this.onSongListener = onSongListener;
            itemView.setOnClickListener(this);
        }

        private void loadCover(String coverUrl){
            this.nivRecommendedCover.setDefaultImageResId(R.drawable.exo_ic_default_album_image);
            this.nivRecommendedCover.setErrorImageResId(R.drawable.exo_ic_default_album_image);

            if (coverUrl==null || coverUrl.isEmpty()) {
                return;
            }

            ImageLoader imageLoader = RequestController.getInstance(context).getImageLoader();

            imageLoader.get(coverUrl, ImageLoader.getImageListener(nivRecommendedCover,
                    R.drawable.exo_ic_default_album_image, android.R.drawable
                            .ic_dialog_alert));
            nivRecommendedCover.setImageUrl(coverUrl, imageLoader);
        }

        void bindData(final Song item) {
            loadCover(item.getCoverUrl());
            this.tvRecSongName.setText(item.getSongName());
        }

        @Override
        public void onClick(View v) {
            this.onSongListener.onRecSongClicked(getBindingAdapterPosition());
        }
    }
    public interface onSongsListener {
        void onRecSongClicked(int pos);
    }
}
