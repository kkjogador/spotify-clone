package com.example.cadastrofacens;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private MusicPlayer musicPlayer;
    private List<Song> songList;
    private static List<Playlist> userPlaylists;
    private List<Song> recentlyPlayedSongs = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private final Handler handler = new Handler();

    // Views do Mini-Player
    private RelativeLayout miniPlayer;
    private ImageView miniPlayerAlbumArt;
    private TextView miniPlayerTitle, miniPlayerArtist;
    private ImageButton miniPlayerPlayPause, miniPlayerLike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Garante que o layout não fique sob a barra de status
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        int themeMode = sharedPreferences.getInt("ThemeMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);

        setContentView(R.layout.activity_main);

        musicPlayer = MusicPlayer.getInstance();

        // UI e Navegação
        miniPlayer = findViewById(R.id.miniPlayer);
        miniPlayerAlbumArt = findViewById(R.id.miniPlayer_album_art);
        miniPlayerTitle = findViewById(R.id.miniPlayer_title);
        miniPlayerArtist = findViewById(R.id.miniPlayer_artist);
        miniPlayerPlayPause = findViewById(R.id.miniPlayer_play_pause_button);
        miniPlayerLike = findViewById(R.id.miniPlayer_like_button);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        // Inicialização
        initializeSongList();
        loadPlaylists(this);
        musicPlayer.setSongList(songList);
        musicPlayer.loadSavedSongsState(this);
        setupPlayerControls();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startMiniPlayerUpdater();
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();
        if (itemId == R.id.navigation_home) {
            selectedFragment = new HomeFragment();
        } else if (itemId == R.id.navigation_search) {
            selectedFragment = new SearchFragment();
        } else if (itemId == R.id.navigation_library) {
            selectedFragment = new LibraryFragment();
        } else if (itemId == R.id.navigation_profile) {
            selectedFragment = new ProfileFragment();
        }
        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        }
        return true;
    };

    private void initializeSongList() {
        songList = new ArrayList<>();
        songList.add(new Song("MTG Fast Food", "Gordão do SN", R.raw.gordaodosn));
        songList.add(new Song("BH é o Califa", "MC Todynho BH", R.raw.mctodynhobhreal));
    }

    private void setupPlayerControls() {
        miniPlayerPlayPause.setOnClickListener(v -> musicPlayer.togglePlayPause());
        miniPlayerLike.setOnClickListener(v -> {
            Song currentSong = musicPlayer.getCurrentSong();
            if (currentSong != null) {
                toggleLikeStatus(currentSong);
            }
        });
        miniPlayer.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NowPlayingActivity.class);
            startActivity(intent);
        });
    }

    private void startMiniPlayerUpdater() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateMiniPlayerUI();
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void updateMiniPlayerUI() {
        Song currentSong = musicPlayer.getCurrentSong();
        if (currentSong != null) {
            miniPlayer.setVisibility(View.VISIBLE);
            miniPlayerTitle.setText(currentSong.getTitle());
            miniPlayerArtist.setText(currentSong.getArtist());
            miniPlayerAlbumArt.setImageResource(R.drawable.ic_album_placeholder);
            miniPlayerPlayPause.setImageResource(musicPlayer.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
            miniPlayerLike.setImageResource(currentSong.isSaved() ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
        } else {
            miniPlayer.setVisibility(View.GONE);
        }
    }

    // --- MÉTODOS PÚBLICOS PARA OS FRAGMENTS ---
    public void playSong(Song song) {
        musicPlayer.playSong(this, song);
        updateRecentlyPlayed(song);
    }

    public List<Song> getSongList() { return songList; }
    public List<Song> getSavedSongs() { return songList.stream().filter(Song::isSaved).collect(Collectors.toList()); }
    public static List<Playlist> getUserPlaylists() { return userPlaylists; }
    public List<Song> getRecentlyPlayedSongs() { return recentlyPlayedSongs; }

    public void createNewPlaylist(String name) {
        userPlaylists.add(new Playlist(name));
        savePlaylists(this);
    }

    public void toggleLikeStatus(Song song) {
        musicPlayer.toggleLikeStatus(this, song);
    }

    public void showSongOptionsMenu(Song song) {
        CharSequence[] items = {"Adicionar à playlist", song.isSaved() ? "Remover das Músicas Curtidas" : "Salvar em Músicas Curtidas"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(song.getTitle());
        builder.setItems(items, (dialog, which) -> {
            if (which == 0) {
                showAddToPlaylistDialog(song);
            } else {
                toggleLikeStatus(song);
            }
        });
        builder.show();
    }

    private void showAddToPlaylistDialog(Song song) {
        List<String> playlistNames = userPlaylists.stream().map(Playlist::getName).collect(Collectors.toList());
        if (playlistNames.isEmpty()) {
            Toast.makeText(this, "Crie uma playlist primeiro na aba Biblioteca", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playlistNames);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar à...");
        builder.setAdapter(adapter, (dialog, which) -> {
            Playlist selectedPlaylist = userPlaylists.get(which);
            selectedPlaylist.addSong(song);
            savePlaylists(this);
            Toast.makeText(this, "Adicionado a " + selectedPlaylist.getName(), Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    // --- MÉTODOS PRIVADOS DE PERSISTÊNCIA ---
    private static void loadPlaylists(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String json = prefs.getString("UserPlaylists", null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<Playlist>>() {}.getType();
            userPlaylists = new Gson().fromJson(json, type);
        } else {
            userPlaylists = new ArrayList<>();
        }
    }

    public static void savePlaylists(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String json = new Gson().toJson(userPlaylists);
        prefs.edit().putString("UserPlaylists", json).apply();
    }
    
    private void updateRecentlyPlayed(Song song) {
        recentlyPlayedSongs.remove(song);
        recentlyPlayedSongs.add(0, song);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }
}
