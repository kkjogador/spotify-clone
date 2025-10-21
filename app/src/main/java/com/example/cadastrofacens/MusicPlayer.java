package com.example.cadastrofacens;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MusicPlayer {

    public enum RepeatMode { OFF, ALL, ONE }

    private static MusicPlayer instance;
    private MediaPlayer mediaPlayer;
    private List<Song> songList = new ArrayList<>();
    private List<Song> shuffledList = new ArrayList<>();
    private int currentSongIndex = -1;
    private boolean isPlaying = false;
    private boolean isShuffleOn = false;
    private RepeatMode repeatMode = RepeatMode.OFF;

    private MusicPlayer() {}

    public static synchronized MusicPlayer getInstance() {
        if (instance == null) {
            instance = new MusicPlayer();
        }
        return instance;
    }

    public void setSongList(List<Song> songs) {
        this.songList = songs;
        this.shuffledList = new ArrayList<>(songs);
        Collections.shuffle(this.shuffledList);
    }

    public void loadSavedSongsState(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        Set<String> savedSongTitles = sharedPreferences.getStringSet("SavedSongs", new HashSet<>());
        for (Song s : songList) {
            s.setSaved(savedSongTitles.contains(s.getTitle()));
        }
    }

    public void toggleLikeStatus(Context context, Song song) {
        song.setSaved(!song.isSaved());
        saveLikedSongsState(context);
    }

    private void saveLikedSongsState(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        Set<String> savedSongTitles = new HashSet<>();
        for (Song s : songList) {
            if (s.isSaved()) {
                savedSongTitles.add(s.getTitle());
            }
        }
        sharedPreferences.edit().putStringSet("SavedSongs", savedSongTitles).apply();
    }

    public Song getCurrentSong() {
        if (currentSongIndex != -1 && !songList.isEmpty()) {
            return isShuffleOn ? shuffledList.get(currentSongIndex) : songList.get(currentSongIndex);
        }
        return null;
    }

    // ... (resto dos métodos: isPlaying, playSong, togglePlayPause, etc.)
    public boolean isPlaying() { return isPlaying; }
    public boolean isShuffleOn() { return isShuffleOn; }
    public RepeatMode getRepeatMode() { return repeatMode; }

    public void playSong(Context context, Song song) {
        List<Song> activeList = isShuffleOn ? shuffledList : songList;
        currentSongIndex = activeList.indexOf(song);
        if (currentSongIndex == -1) return;

        if (mediaPlayer != null) mediaPlayer.release();

        mediaPlayer = MediaPlayer.create(context, song.getFileResId());
        mediaPlayer.setOnCompletionListener(mp -> playNext(context));
        mediaPlayer.start();
        isPlaying = true;
    }

    public void togglePlayPause() {
        if (mediaPlayer == null) return;
        if (isPlaying) mediaPlayer.pause();
        else mediaPlayer.start();
        isPlaying = !isPlaying;
    }

    public void playNext(Context context) {
        if (songList.isEmpty()) return;

        if (repeatMode == RepeatMode.ONE) {
            playSong(context, getCurrentSong()); 
            return;
        }

        List<Song> activeList = isShuffleOn ? shuffledList : songList;
        currentSongIndex++;

        if (currentSongIndex >= activeList.size()) {
            if (repeatMode == RepeatMode.ALL) {
                currentSongIndex = 0; 
            } else {
                mediaPlayer.stop();
                isPlaying = false;
                currentSongIndex = -1;
                return;
            }
        }
        playSong(context, activeList.get(currentSongIndex));
    }

    public void playPrevious(Context context) {
        if (songList.isEmpty()) return;
        List<Song> activeList = isShuffleOn ? shuffledList : songList;
        currentSongIndex = (currentSongIndex - 1 + activeList.size()) % activeList.size();
        playSong(context, activeList.get(currentSongIndex));
    }
    
    public void toggleShuffle() {
        isShuffleOn = !isShuffleOn;
        if (isShuffleOn) {
            Collections.shuffle(shuffledList);
        }
    }

    public void cycleRepeatMode() {
        if (repeatMode == RepeatMode.OFF) repeatMode = RepeatMode.ALL;
        else if (repeatMode == RepeatMode.ALL) repeatMode = RepeatMode.ONE;
        else repeatMode = RepeatMode.OFF;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}
