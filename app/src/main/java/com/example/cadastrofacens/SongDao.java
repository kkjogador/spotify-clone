package com.example.cadastrofacens;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SongDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(Song... songs);

    @Query("SELECT * FROM songs")
    List<Song> getAllSongs();

    @Query("SELECT COUNT(*) FROM songs")
    int getSongCount();
}
