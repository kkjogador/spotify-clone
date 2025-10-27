package com.example.cadastrofacens;

import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(primaryKeys = {"playlistId", "songId"},
        foreignKeys = {
                @ForeignKey(entity = Playlist.class,
                            parentColumns = "id",
                            childColumns = "playlistId",
                            onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Song.class,
                            parentColumns = "id",
                            childColumns = "songId",
                            onDelete = ForeignKey.CASCADE)
        })
public class PlaylistSongCrossRef {
    public int playlistId;
    public int songId;

    public PlaylistSongCrossRef(int playlistId, int songId) {
        this.playlistId = playlistId;
        this.songId = songId;
    }
}
