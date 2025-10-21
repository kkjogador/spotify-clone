package com.example.cadastrofacens;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songList;
    private final List<Song> songListFull;
    private final OnSongInteractionListener listener;

    public interface OnSongInteractionListener {
        void onSongClick(Song song);
        void onOptionsMenuClick(Song song);
    }

    public SongAdapter(List<Song> songList, OnSongInteractionListener listener) {
        this.songList = songList;
        this.songListFull = new ArrayList<>(songList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.txtSongTitle.setText(song.getTitle());
        holder.txtSongArtist.setText(song.getArtist());

        holder.albumArt.setImageResource(R.drawable.ic_album_placeholder);

        // Listener para tocar a música (no item inteiro)
        holder.itemView.setOnClickListener(v -> listener.onSongClick(song));

        // Listener para o menu de opções
        holder.optionsMenu.setOnClickListener(v -> listener.onOptionsMenuClick(song));
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public void filter(String text) {
        songList.clear();
        if (text.isEmpty()) {
            songList.addAll(songListFull);
        } else {
            text = text.toLowerCase();
            for (Song song : songListFull) {
                if (song.getTitle().toLowerCase().contains(text) || song.getArtist().toLowerCase().contains(text)) {
                    songList.add(song);
                }
            }
        }
        notifyDataSetChanged();
    }
    
    public void updateList(List<Song> newList) {
        songList.clear();
        songList.addAll(newList);
        songListFull.clear();
        songListFull.addAll(newList);
        notifyDataSetChanged();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView txtSongTitle;
        TextView txtSongArtist;
        ImageButton optionsMenu;
        ImageView albumArt;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSongTitle = itemView.findViewById(R.id.txtSongTitle);
            txtSongArtist = itemView.findViewById(R.id.txtSongArtist);
            optionsMenu = itemView.findViewById(R.id.options_menu);
            albumArt = itemView.findViewById(R.id.album_art);
        }
    }
}
