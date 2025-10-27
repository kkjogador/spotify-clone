package com.example.cadastrofacens;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LibraryFragment extends Fragment implements PlaylistAdapter.OnPlaylistClickListener {

    private RecyclerView recyclerView;
    private PlaylistAdapter playlistAdapter;
    private MainViewModel mainViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtém o ViewModel que é compartilhado com a MainActivity
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewPlaylists);
        FloatingActionButton fab = view.findViewById(R.id.fab_add_playlist);

        fab.setOnClickListener(v -> showCreatePlaylistDialog());

        setupRecyclerView();
        observeViewModel();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // O adapter é inicializado com uma lista vazia e atualizado pelo LiveData
        playlistAdapter = new PlaylistAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(playlistAdapter);
    }

    // O LibraryFragment agora observa as mudanças nos dados do ViewModel
    private void observeViewModel() {
        mainViewModel.getUserPlaylistsWithSongs().observe(getViewLifecycleOwner(), playlistsWithSongs -> {
            if (playlistsWithSongs != null) {
                List<Playlist> allPlaylists = new ArrayList<>();
                // Adiciona a playlist "Músicas Curtidas" no topo
                Playlist likedSongsPlaylist = new Playlist("Músicas Curtidas");
                // TODO: Adicionar lógica para carregar músicas curtidas do banco
                allPlaylists.add(likedSongsPlaylist);

                // Processa e adiciona as playlists do usuário
                List<Playlist> userPlaylists = playlistsWithSongs.stream().map(pws -> {
                    pws.playlist.setSongs(pws.songs);
                    return pws.playlist;
                }).collect(Collectors.toList());
                allPlaylists.addAll(userPlaylists);

                // Atualiza o adapter
                playlistAdapter.updatePlaylists(allPlaylists);
            }
        });
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
                // A criação agora é delegada ao ViewModel
                mainViewModel.createNewPlaylist(playlistName);
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onPlaylistClick(Playlist playlist) {
        Intent intent = new Intent(getActivity(), PlaylistActivity.class);
        intent.putExtra("playlist", playlist);
        startActivity(intent);
    }
}
