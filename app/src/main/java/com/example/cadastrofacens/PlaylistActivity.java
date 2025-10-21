package com.example.cadastrofacens;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistActivity extends AppCompatActivity implements SongAdapter.OnSongInteractionListener {

    private Playlist playlist;
    private MusicPlayer musicPlayer;
    private SongAdapter adapter; // Tornando o adapter uma variável de classe

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        musicPlayer = MusicPlayer.getInstance();
        playlist = (Playlist) getIntent().getSerializableExtra("playlist");

        Toolbar toolbar = findViewById(R.id.playlist_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(playlist.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.playlist_songs_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SongAdapter(playlist.getSongs(), this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onSongClick(Song song) {
        // Toca a música e define a fila de reprodução para esta playlist
        musicPlayer.setSongList(playlist.getSongs());
        musicPlayer.playSong(this, song);
    }

    // MÉTODO FALTANTE ADICIONADO
    @Override
    public void onOptionsMenuClick(Song song) {
        // Ação principal do menu aqui é curtir/descurtir
        musicPlayer.toggleLikeStatus(this, song);
        // Notifica o adapter para que o ícone de coração (se visível) atualize
        adapter.notifyDataSetChanged(); 
    }
}
