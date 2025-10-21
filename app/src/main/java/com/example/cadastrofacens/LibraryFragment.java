package com.example.cadastrofacens;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

// 1. Implementar a interface correta
public class LibraryFragment extends Fragment implements PlaylistAdapter.OnPlaylistClickListener {

    private RecyclerView recyclerView;
    private PlaylistAdapter playlistAdapter;
    private List<Playlist> allPlaylists;
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
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewPlaylists);
        FloatingActionButton fab = view.findViewById(R.id.fab_add_playlist);

        fab.setOnClickListener(v -> showCreatePlaylistDialog());
        
        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadPlaylists();
    }

    private void loadPlaylists() {
        if (mainActivity == null) return;

        allPlaylists = new ArrayList<>();
        Playlist likedSongsPlaylist = new Playlist("Músicas Curtidas");
        mainActivity.getSavedSongs().forEach(likedSongsPlaylist::addSong);
        allPlaylists.add(likedSongsPlaylist);

        allPlaylists.addAll(mainActivity.getUserPlaylists());

        // 2. Passar "this" como listener
        playlistAdapter = new PlaylistAdapter(allPlaylists, this);
        recyclerView.setAdapter(playlistAdapter);
    }

    private void showCreatePlaylistDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Nova Playlist");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Nome da Playlist");
        builder.setView(input);

        builder.setPositiveButton("Criar", (dialog, which) -> {
            String playlistName = input.getText().toString();
            if (!playlistName.isEmpty()) {
                mainActivity.createNewPlaylist(playlistName);
                loadPlaylists(); 
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // 3. Implementar o método de clique que faltava
    @Override
    public void onPlaylistClick(Playlist playlist) {
        Intent intent = new Intent(getActivity(), PlaylistActivity.class);
        intent.putExtra("playlist", playlist);
        startActivity(intent);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (playlistAdapter != null) {
            loadPlaylists();
        }
    }
}
