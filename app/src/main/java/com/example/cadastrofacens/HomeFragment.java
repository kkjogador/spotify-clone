package com.example.cadastrofacens;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment implements SongAdapter.OnSongInteractionListener, HomeGridAdapter.OnPlaylistClickListener {

    private RecyclerView quickAccessGrid;
    private RecyclerView recentlyPlayedRecyclerView;
    private HomeGridAdapter gridAdapter;
    private SongAdapter songAdapter;
    private TextView greetingText;
    private TextView txtEmptyState;
    private MainActivity mainActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mainActivity = (MainActivity) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Encontrando as Views do layout
        quickAccessGrid = view.findViewById(R.id.quick_access_grid);
        recentlyPlayedRecyclerView = view.findViewById(R.id.recyclerViewRecentlyPlayed);
        greetingText = view.findViewById(R.id.greeting_text);
        txtEmptyState = view.findViewById(R.id.txt_empty_state);
        ImageButton settingsButton = view.findViewById(R.id.settings_button);

        settingsButton.setOnClickListener(v -> {
            // Lógica para abrir configurações aqui
        });

        setGreeting();
        setupQuickAccessGrid();
        loadRecentlyPlayed(); // Chamada para o método restaurado

        return view;
    }

    private void setGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 0 && hour < 12) {
            greetingText.setText("Bom dia");
        } else if (hour >= 12 && hour < 18) {
            greetingText.setText("Boa tarde");
        } else {
            greetingText.setText("Boa noite");
        }
    }

    private void setupQuickAccessGrid() {
        List<Playlist> quickAccessPlaylists = new ArrayList<>();
        Playlist likedSongsPlaylist = new Playlist("Músicas Curtidas");
        if (mainActivity != null) {
            mainActivity.getSavedSongs().forEach(likedSongsPlaylist::addSong);
        }
        quickAccessPlaylists.add(likedSongsPlaylist);
        
        gridAdapter = new HomeGridAdapter(quickAccessPlaylists, this);
        quickAccessGrid.setLayoutManager(new GridLayoutManager(getContext(), 2, GridLayoutManager.HORIZONTAL, false));
        quickAccessGrid.setAdapter(gridAdapter);
    }

    // MÉTODO RESTAURADO
    private void loadRecentlyPlayed() {
        if (mainActivity == null) return;
        
        List<Song> recentlyPlayed = mainActivity.getRecentlyPlayedSongs();
        if (recentlyPlayed.isEmpty()) {
            if(txtEmptyState != null) txtEmptyState.setVisibility(View.VISIBLE);
            if(recentlyPlayedRecyclerView != null) recentlyPlayedRecyclerView.setVisibility(View.GONE);
        } else {
            if(txtEmptyState != null) txtEmptyState.setVisibility(View.GONE);
            if(recentlyPlayedRecyclerView != null) {
                recentlyPlayedRecyclerView.setVisibility(View.VISIBLE);
                songAdapter = new SongAdapter(recentlyPlayed, this);
                recentlyPlayedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recentlyPlayedRecyclerView.setAdapter(songAdapter);
            }
        }
    }

    // --- MÉTODOS DE INTERAÇÃO COM AS LISTAS ---

    @Override
    public void onSongClick(Song song) {
        if (mainActivity != null) {
            mainActivity.playSong(song);
        }
    }

    @Override
    public void onOptionsMenuClick(Song song) {
        if (mainActivity != null) {
            mainActivity.showSongOptionsMenu(song);
        }
    }

    @Override
    public void onPlaylistClick(Playlist playlist) {
        Intent intent = new Intent(getActivity(), PlaylistActivity.class);
        intent.putExtra("playlist", playlist);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecentlyPlayed(); // Chamada para o método restaurado
    }
}
