package com.example.cadastrofacens;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import java.util.Collections;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final MutableLiveData<String> userIdTrigger = new MutableLiveData<>();

    private final LiveData<List<PlaylistWithSongs>> userPlaylistsWithSongs;
    private final LiveData<List<Integer>> recentlyPlayedSongIds;

    public MainViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);

        // Quando o userIdTrigger muda, esta transformação automaticamente busca os novos dados no banco
        userPlaylistsWithSongs = Transformations.switchMap(userIdTrigger, userId -> {
            if (userId == null) {
                // Retorna um LiveData vazio se não houver usuário
                return new MutableLiveData<>(Collections.emptyList());
            }
            return db.playlistDao().getUserPlaylistsWithSongs(userId);
        });

        recentlyPlayedSongIds = Transformations.switchMap(userIdTrigger, userId -> {
            if (userId == null) {
                return new MutableLiveData<>(Collections.emptyList());
            }
            return db.playedSongDao().getRecentlyPlayedSongIds(userId);
        });
    }

    // Este é o gatilho para carregar/limpar os dados de um usuário
    public void setUser(String userId) {
        if (userId != null && userId.equals(userIdTrigger.getValue())) {
            return; // Evita recarregar desnecessariamente
        }
        userIdTrigger.setValue(userId);
    }

    // Getters para os Fragments observarem
    public LiveData<List<PlaylistWithSongs>> getUserPlaylistsWithSongs() {
        return userPlaylistsWithSongs;
    }

    public LiveData<List<Integer>> getRecentlyPlayedSongIds() {
        return recentlyPlayedSongIds;
    }

    // Ações que escrevem no banco
    public void createNewPlaylist(String name) {
        String currentUserId = userIdTrigger.getValue();
        if (currentUserId != null) {
            Playlist newPlaylist = new Playlist(name, currentUserId);
            AppDatabase.databaseWriteExecutor.execute(() -> db.playlistDao().createPlaylist(newPlaylist));
        }
    }

    public void addSongToHistory(Song song) {
        String currentUserId = userIdTrigger.getValue();
        if (currentUserId != null && song != null) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                db.playedSongDao().delete(currentUserId, song.id);
                PlayedSong playedSong = new PlayedSong(currentUserId, song.id, System.currentTimeMillis());
                db.playedSongDao().insert(playedSong);
            });
        }
    }
}
