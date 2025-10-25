package com.example.cadastrofacens;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NowPlayingActivity extends AppCompatActivity {

    private MusicPlayer musicPlayer;
    private TextView title, artist, currentTime, totalDuration;
    private ImageView albumArt;
    private ImageButton playPause, prev, next, downArrow, shuffle, repeat, like, addToPlaylistButton;
    private SeekBar seekBar;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        // Correção definitiva para a sobreposição da barra de status
        applyStatusBarPadding();

        musicPlayer = MusicPlayer.getInstance();

        title = findViewById(R.id.now_playing_title);
        artist = findViewById(R.id.now_playing_artist);
        currentTime = findViewById(R.id.now_playing_current_time);
        totalDuration = findViewById(R.id.now_playing_total_duration);
        albumArt = findViewById(R.id.now_playing_album_art);
        playPause = findViewById(R.id.now_playing_play_pause);
        prev = findViewById(R.id.now_playing_skip_previous);
        next = findViewById(R.id.now_playing_skip_next);
        seekBar = findViewById(R.id.now_playing_seekbar);
        downArrow = findViewById(R.id.down_arrow);
        shuffle = findViewById(R.id.now_playing_shuffle);
        repeat = findViewById(R.id.now_playing_repeat);
        like = findViewById(R.id.now_playing_like_button);
        addToPlaylistButton = findViewById(R.id.now_playing_add_playlist_button);

        downArrow.setOnClickListener(v -> finish());
        playPause.setOnClickListener(v -> musicPlayer.togglePlayPause());
        next.setOnClickListener(v -> musicPlayer.playNext(this));
        prev.setOnClickListener(v -> musicPlayer.playPrevious(this));
        shuffle.setOnClickListener(v -> musicPlayer.toggleShuffle());
        repeat.setOnClickListener(v -> musicPlayer.cycleRepeatMode());

        like.setOnClickListener(v -> {
            Song currentSong = musicPlayer.getCurrentSong();
            if (currentSong != null) {
                musicPlayer.toggleLikeStatus(this, currentSong);
            }
        });

        addToPlaylistButton.setOnClickListener(v -> showAddToPlaylistDialog());

        setupSeekBar();
    }

    private void applyStatusBarPadding() {
        View container = findViewById(R.id.now_playing_container);
        container.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            container.getWindowVisibleDisplayFrame(r);
            int statusBarHeight = r.top;
            container.setPadding(0, statusBarHeight, 0, 0);
        });
    }

    private void showAddToPlaylistDialog() {
        List<Playlist> userPlaylists = MainActivity.getUserPlaylists();
        Song currentSong = musicPlayer.getCurrentSong();

        if (currentSong == null) return;

        if (userPlaylists == null || userPlaylists.isEmpty()) {
            Toast.makeText(this, "Nenhuma playlist criada", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] playlistNames = new String[userPlaylists.size()];
        for (int i = 0; i < userPlaylists.size(); i++) {
            playlistNames[i] = userPlaylists.get(i).getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar à playlist")
                .setItems(playlistNames, (dialog, which) -> {
                    Playlist selectedPlaylist = userPlaylists.get(which);
                    selectedPlaylist.addSong(currentSong);
                    MainActivity.savePlaylists(this); // Salva as playlists
                    Toast.makeText(NowPlayingActivity.this, "Adicionado a " + selectedPlaylist.getName(), Toast.LENGTH_SHORT).show();
                });

        builder.create().show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
        startUIUpdater();
    }

    private void updateUI() {
        Song currentSong = musicPlayer.getCurrentSong();
        if (currentSong == null) {
            finish();
            return;
        }

        title.setText(currentSong.getTitle());
        artist.setText(currentSong.getArtist());
        albumArt.setImageResource(R.drawable.ic_album_placeholder);
        playPause.setImageResource(musicPlayer.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
        like.setImageResource(currentSong.isSaved() ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);

        updateShuffleButton();
        updateRepeatButton();

        if (musicPlayer.getMediaPlayer() != null) {
            int duration = musicPlayer.getMediaPlayer().getDuration();
            seekBar.setMax(duration);
            totalDuration.setText(formatDuration(duration));
        }
    }

    private void startUIUpdater() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicPlayer.getMediaPlayer() != null) {
                    int currentPosition = musicPlayer.getMediaPlayer().getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    currentTime.setText(formatDuration(currentPosition));
                }
                updateUI();
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && musicPlayer.getMediaPlayer() != null) {
                    musicPlayer.getMediaPlayer().seekTo(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateShuffleButton() {
        if (musicPlayer.isShuffleOn()) {
            shuffle.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.spotify_green)));
        } else {
            shuffle.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_primary_dark)));
        }
    }

    private void updateRepeatButton() {
        switch (musicPlayer.getRepeatMode()) {
            case OFF:
                repeat.setImageResource(R.drawable.ic_repeat);
                repeat.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_primary_dark)));
                break;
            case ALL:
                repeat.setImageResource(R.drawable.ic_repeat);
                repeat.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.spotify_green)));
                break;
            case ONE:
                repeat.setImageResource(R.drawable.ic_repeat_one);
                repeat.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.spotify_green)));
                break;
        }
    }

    private String formatDuration(long duration) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }
}
