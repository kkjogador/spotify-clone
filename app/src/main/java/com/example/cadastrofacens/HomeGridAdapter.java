package com.example.cadastrofacens;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// Adaptador restaurado para funcionar com Playlists
public class HomeGridAdapter extends RecyclerView.Adapter<HomeGridAdapter.GridViewHolder> {

    private final List<Playlist> playlists;
    private final OnPlaylistClickListener listener;

    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist);
    }

    public HomeGridAdapter(List<Playlist> playlists, OnPlaylistClickListener listener) {
        this.playlists = playlists;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_grid_item, parent, false);
        return new GridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);
        holder.title.setText(playlist.getName());

        // Lógica para mostrar o coração verde para a playlist "Músicas Curtidas"
        if (playlist.getName().equals("Músicas Curtidas")) {
            holder.image.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            holder.image.setImageResource(R.drawable.ic_album_placeholder);
        }

        holder.itemView.setOnClickListener(v -> listener.onPlaylistClick(playlist));
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public static class GridViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;

        public GridViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.grid_item_image);
            title = itemView.findViewById(R.id.grid_item_title);
        }
    }
}
