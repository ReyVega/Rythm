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

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private List<ListElement> items;
    private LayoutInflater inflater;
    private Context context;

    public ListAdapter(List<ListElement> items, Context context) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.items = items;
    }

    @Override
    public int getItemCount() {
        return this.items == null ? 0 : this.items.size();
    }

    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = this.inflater.inflate(R.layout.list_element, null);
        return new ListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ListAdapter.ViewHolder holder, final int position) {
        holder.bindData(this.items.get(position));
    }

    public void setItems(List<ListElement> items) {
        this.items = items;
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

        void bindData(final ListElement item) {
            this.iconImage.setColorFilter(Color.parseColor(item.getColor()), PorterDuff.Mode.SRC_IN);
            this.songName.setText(item.getSongName());
            this.artistName.setText(item.getArtistName());
            this.genreName.setText(item.getGenreName());
        }
    }
}
