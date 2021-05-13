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

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {
    private List<Playlist> playlists;
    private LayoutInflater inflater;
    private Context context;
    private onPlayListListener onPlayListListener;

    public LibraryAdapter(List<Playlist> playlists, Context context, onPlayListListener onPlayListListener) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.playlists = playlists;
        this.onPlayListListener = onPlayListListener;
    }

    @Override
    public int getItemCount() {
        return this.playlists == null ? 0 : this.playlists.size();
    }

    @Override
    public LibraryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = this.inflater.inflate(R.layout.playlist_element, null);
        return new LibraryAdapter.ViewHolder(v, this.onPlayListListener);
    }

    @Override
    public void onBindViewHolder(final LibraryAdapter.ViewHolder holder, final int position) {
        holder.bindData(this.playlists.get(position));
    }

    public void setPlaylist(List<Playlist> playlists) {
        this.playlists = playlists;
    }

    public void addPlayList(Playlist playlist) {
        this.playlists.add(playlist);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView iconImage;
        TextView playListName;
        onPlayListListener onPlayListListener;

        ViewHolder(View itemView, onPlayListListener onPlayListListener) {
            super(itemView);
            this.iconImage = itemView.findViewById(R.id.iconImageView);
            this.playListName = itemView.findViewById(R.id.playListName);
            this.onPlayListListener = onPlayListListener;
            itemView.setOnClickListener(this);
        }

        void bindData(final Playlist item) {
            this.iconImage.setColorFilter(Color.parseColor("#0000FF"), PorterDuff.Mode.SRC_IN);
            this.playListName.setText(item.getName());
        }

        @Override
        public void onClick(View v) {
            this.onPlayListListener.onItemClick(getAdapterPosition());
        }
    }
    public interface onPlayListListener {
        void onItemClick(int pos);
    }
}
