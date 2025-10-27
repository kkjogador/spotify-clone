package com.example.cadastrofacens;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PlayedSongDao {

    @Insert
    void insert(PlayedSong playedSong);

    // CORRIGIDO: Retornando LiveData para a arquitetura ViewModel
    @Query("SELECT songId FROM played_history WHERE userId = :userId ORDER BY timestamp DESC")
    LiveData<List<Integer>> getRecentlyPlayedSongIds(String userId);

    @Query("DELETE FROM played_history WHERE userId = :userId AND songId = :songId")
    void delete(String userId, int songId);

    @Query("SELECT COUNT(*) FROM played_history WHERE userId = :userId")
    int getHistoryCountForUser(String userId);
}
