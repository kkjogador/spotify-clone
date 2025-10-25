package com.example.cadastrofacens;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistActivity extends AppCompatActivity implements SongAdapter.OnSongInteractionListener {

    private Playlist playlist;
    private MusicPlayer musicPlayer;
    private SongAdapter adapter;
    private final Handler handler = new Handler();

    // Views do Mini-Player
    private RelativeLayout miniPlayer;
    private ImageView miniPlayerAlbumArt;
    private TextView miniPlayerTitle, miniPlayerArtist;
    private ImageButton miniPlayerPlayPause, miniPlayerLike;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.activity_playlist);

        musicPlayer = MusicPlayer.getInstance();
        playlist = (Playlist) getIntent().getSerializableExtra("playlist");

        // Toolbar
        Toolbar toolbar = findViewById(R.id.playlist_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(playlist.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // RecyclerView
        RecyclerView recyclerView = findViewById(R.id.playlist_songs_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SongAdapter(playlist.getSongs(), this);
        recyclerView.setAdapter(adapter);

        // Swipe to delete
        setUpItemTouchHelper(recyclerView);

        // Mini-Player
        miniPlayer = findViewById(R.id.miniPlayer);
        miniPlayerAlbumArt = findViewById(R.id.miniPlayer_album_art);
        miniPlayerTitle = findViewById(R.id.miniPlayer_title);
        miniPlayerArtist = findViewById(R.id.miniPlayer_artist);
        miniPlayerPlayPause = findViewById(R.id.miniPlayer_play_pause_button);
        miniPlayerLike = findViewById(R.id.miniPlayer_like_button);
        setupPlayerControls();
    }

    private void setUpItemTouchHelper(RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            private final Drawable deleteIcon = ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.ic_delete);
            private final ColorDrawable background = new ColorDrawable(Color.RED);

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // Não usamos para reordenar
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final Song songToRemove = playlist.getSongs().get(position);

                new AlertDialog.Builder(PlaylistActivity.this)
                        .setTitle("Excluir Música")
                        .setMessage("Tem certeza que deseja remover \"" + songToRemove.getTitle() + "\" da playlist?")
                        .setPositiveButton("Excluir", (dialog, which) -> {
                            playlist.getSongs().remove(position);
                            adapter.notifyItemRemoved(position);
                            MainActivity.savePlaylists(PlaylistActivity.this);
                        })
                        .setNegativeButton("Cancelar", (dialog, which) -> {
                            adapter.notifyItemChanged(position);
                        })
                        .setOnCancelListener(dialog -> {
                            adapter.notifyItemChanged(position);
                        })
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                View itemView = viewHolder.itemView;
                int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                int iconTop = itemView.getTop() + (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();

                if (dX > 0) { // Arrastando para a direita
                    int iconLeft = itemView.getLeft() + iconMargin;
                    int iconRight = itemView.getLeft() + iconMargin + deleteIcon.getIntrinsicWidth();
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                    background.setBounds(itemView.getLeft(), itemView.getTop(),
                            itemView.getLeft() + ((int) dX), itemView.getBottom());
                } else { // Não arrastando
                    background.setBounds(0, 0, 0, 0);
                    deleteIcon.setBounds(0,0,0,0);
                }
                background.draw(c);
                deleteIcon.draw(c);
            }
        };
        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startMiniPlayerUpdater();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    private void setupPlayerControls() {
        miniPlayerPlayPause.setOnClickListener(v -> musicPlayer.togglePlayPause());
        miniPlayerLike.setOnClickListener(v -> {
            Song currentSong = musicPlayer.getCurrentSong();
            if (currentSong != null) {
                musicPlayer.toggleLikeStatus(this, currentSong);
            }
        });
        miniPlayer.setOnClickListener(v -> {
            Intent intent = new Intent(PlaylistActivity.this, NowPlayingActivity.class);
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

    @Override
    public void onSongClick(Song song) {
        musicPlayer.setSongList(playlist.getSongs());
        musicPlayer.playSong(this, song);
    }

    @Override
    public void onOptionsMenuClick(Song song) {
        musicPlayer.toggleLikeStatus(this, song);
        adapter.notifyDataSetChanged();
    }
}
