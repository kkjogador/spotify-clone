package com.example.cadastrofacens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private MusicPlayer musicPlayer;
    private AppDatabase db;
    private List<Song> allSongs = new ArrayList<>();

    private final Handler handler = new Handler();

    private RelativeLayout miniPlayer;
    private ImageView miniPlayerAlbumArt;
    private TextView miniPlayerTitle, miniPlayerArtist;
    private ImageButton miniPlayerPlayPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.activity_main);

        // ViewModel é o novo cérebro para os dados
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        db = AppDatabase.getDatabase(this);
        musicPlayer = MusicPlayer.getInstance();

        setupViews();
        setupPlayerControls();
        loadInitialData();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }
    }

    private void setupViews(){
        miniPlayer = findViewById(R.id.miniPlayer);
        miniPlayerAlbumArt = findViewById(R.id.miniPlayer_album_art);
        miniPlayerTitle = findViewById(R.id.miniPlayer_title);
        miniPlayerArtist = findViewById(R.id.miniPlayer_artist);
        miniPlayerPlayPause = findViewById(R.id.miniPlayer_play_pause_button);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);
    }

    // Carrega dados que não dependem do usuário
    private void loadInitialData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            allSongs = db.songDao().getAllSongs();
            musicPlayer.setSongList(allSongs);
            // Após carregar as músicas, verifica o estado do usuário
            runOnUiThread(this::checkUserStatus);
        });
    }

    // A MainActivity agora apenas INFORMA o ViewModel sobre o estado do usuário
    public void checkUserStatus() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            mainViewModel.setUser(currentUser.getUid());
        } else {
            mainViewModel.setUser(null); // Limpa os dados no ViewModel
        }
    }

    public void playSong(Song song) {
        musicPlayer.playSong(this, song);
        mainViewModel.addSongToHistory(song);
    }
    
    // Os fragments agora pegarão o ViewModel diretamente
    public List<Song> getAllSongs() { return allSongs; }

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

    private void setupPlayerControls() {
        miniPlayerPlayPause.setOnClickListener(v -> musicPlayer.togglePlayPause());
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
        } else {
            miniPlayer.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startMiniPlayerUpdater();
        checkUserStatus(); // Garante que o estado do usuário seja verificado ao voltar para o app
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }
}
