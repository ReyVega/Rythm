package com.example.rythm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecommendedSongsAdapter extends RecyclerView.Adapter<RecommendedSongsAdapter.ViewHolder> {

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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView iconImage;
        TextView tvRecSongName;
        onSongsListener onSongListener;

        ViewHolder(View itemView, onSongsListener onSongListener) {
            super(itemView);
            this.iconImage = itemView.findViewById(R.id.iconImageView);
            this.tvRecSongName = itemView.findViewById(R.id.tvRecSong);
            this.onSongListener = onSongListener;
            itemView.setOnClickListener(this);
        }

        void bindData(final Song item) {
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
