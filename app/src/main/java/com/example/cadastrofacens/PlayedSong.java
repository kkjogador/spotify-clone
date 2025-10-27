package com.example.cadastrofacens;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "played_history",
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Song.class,
                        parentColumns = "id",
                        childColumns = "songId",
                        onDelete = ForeignKey.CASCADE)
        })
public class PlayedSong {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String userId;
    public int songId;
    public long timestamp;

    public PlayedSong(String userId, int songId, long timestamp) {
        this.userId = userId;
        this.songId = songId;
        this.timestamp = timestamp;
    }
}
