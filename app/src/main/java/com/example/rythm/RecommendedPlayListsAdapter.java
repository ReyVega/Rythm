package com.example.rythm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecommendedPlayListsAdapter extends RecyclerView.Adapter<RecommendedPlayListsAdapter.ViewHolder> {

    private List<Playlist> songs;
    private LayoutInflater inflater;
    private Context context;
    private RecommendedPlayListsAdapter.onPlayListListener onPlayListListener;

    public RecommendedPlayListsAdapter(List<Playlist> songs, Context context, RecommendedPlayListsAdapter.onPlayListListener onPlayListListener) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.songs = songs;
        this.onPlayListListener = onPlayListListener;
    }

    @Override
    public int getItemCount() {
        return this.songs == null ? 0 : this.songs.size();
    }

    @Override
    public RecommendedPlayListsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = this.inflater.inflate(R.layout.recommended_playlist, null);
        return new RecommendedPlayListsAdapter.ViewHolder(v, this.onPlayListListener);
    }

    @Override
    public void onBindViewHolder(final RecommendedPlayListsAdapter.ViewHolder holder, final int position) {
        holder.bindData(this.songs.get(position));
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView iconImage;
        TextView tvRecPlayList;
        RecommendedPlayListsAdapter.onPlayListListener onPlayListListener;

        ViewHolder(View itemView, RecommendedPlayListsAdapter.onPlayListListener onPlayListListener) {
            super(itemView);
            this.iconImage = itemView.findViewById(R.id.iconImageView);
            this.tvRecPlayList = itemView.findViewById(R.id.tvRecPlayList);
            this.onPlayListListener = onPlayListListener;
            itemView.setOnClickListener(this);
        }

        void bindData(final Playlist playlist) {
            this.tvRecPlayList.setText(playlist.getName());
        }

        @Override
        public void onClick(View v) {
            this.onPlayListListener.onRecPlayListClick(getBindingAdapterPosition());
        }
    }
    public interface onPlayListListener {
        void onRecPlayListClick(int pos);
    }
}
