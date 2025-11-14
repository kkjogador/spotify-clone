package com.example.cadastrofacens;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment implements PlaylistAdapter.OnPlaylistClickListener {

    private RecyclerView recyclerView;
    private PlaylistAdapter playlistAdapter;

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
        // O adapter agora é inicializado com uma lista estática de playlists
        playlistAdapter = new PlaylistAdapter(createStaticPlaylists(), this);
        recyclerView.setAdapter(playlistAdapter);
    }

    private List<Playlist> createStaticPlaylists() {
        List<Playlist> playlists = new ArrayList<>();

        // Playlist "Músicas Curtidas"
        Playlist likedSongsPlaylist = new Playlist("Músicas Curtidas");
        List<Song> likedSongs = new ArrayList<>();
        likedSongs.add(new Song("Bohemian Rhapsody", "Queen", R.drawable.ic_album_placeholder));
        likedSongs.add(new Song("Stairway to Heaven", "Led Zeppelin", R.drawable.ic_album_placeholder));
        likedSongs.add(new Song("Hotel California", "Eagles", R.drawable.ic_album_placeholder));
        likedSongsPlaylist.setSongs(likedSongs);
        playlists.add(likedSongsPlaylist);

        // Playlist de Rock
        Playlist rockPlaylist = new Playlist("Meu Rock Favorito");
        List<Song> rockSongs = new ArrayList<>();
        rockSongs.add(new Song("Back In Black", "AC/DC", R.drawable.ic_album_placeholder));
        rockSongs.add(new Song("Smells Like Teen Spirit", "Nirvana", R.drawable.ic_album_placeholder));
        rockPlaylist.setSongs(rockSongs);
        playlists.add(rockPlaylist);

        // Playlist de Pop (vazia)
        Playlist popPlaylist = new Playlist("Top Brasil");
        popPlaylist.setSongs(new ArrayList<>());
        playlists.add(popPlaylist);

        return playlists;
    }

    private void showCreatePlaylistDialog() {
        // Funcionalidade desativada temporariamente enquanto usamos dados estáticos
        Toast.makeText(getContext(), "Criação de playlist desativada em modo de dados estáticos.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPlaylistClick(Playlist playlist) {
        Intent intent = new Intent(getActivity(), PlaylistActivity.class);
        // O objeto playlist agora contém a lista de músicas estáticas
        intent.putExtra("playlist", playlist);
        startActivity(intent);
    }
}
