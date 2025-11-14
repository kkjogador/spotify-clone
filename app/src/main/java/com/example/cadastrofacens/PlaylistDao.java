package com.example.cadastrofacens;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long createPlaylist(Playlist playlist);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addSongToPlaylist(PlaylistSongCrossRef crossRef);

    @Transaction
    @Query("SELECT * FROM playlists WHERE userId = :userId")
    LiveData<List<PlaylistWithSongs>> getUserPlaylistsWithSongs(String userId);

    @Transaction
    @Query("SELECT * FROM playlists")
    LiveData<List<PlaylistWithSongs>> getAllPlaylistsWithSongs();

    @Query("DELETE FROM PlaylistSongCrossRef WHERE playlistId = :playlistId AND songId = :songId")
    void removeSongFromPlaylist(int playlistId, int songId);

    @Query("SELECT COUNT(*) FROM playlists WHERE userId = :userId")
    int getPlaylistCountForUser(String userId);

    @Query("SELECT * FROM playlists")
    List<Playlist> getAllPlaylists();

    @Query("SELECT * FROM PlaylistSongCrossRef")
    List<PlaylistSongCrossRef> getAllCrossRefs();
}
