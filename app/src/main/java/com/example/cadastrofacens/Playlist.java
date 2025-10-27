package com.example.cadastrofacens;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "playlists",
        foreignKeys = @ForeignKey(entity = User.class,
                                  parentColumns = "id",
                                  childColumns = "userId",
                                  onDelete = ForeignKey.CASCADE))
public class Playlist implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    private String name;

    @ColumnInfo(index = true) // Melhora a performance de buscas por usuário
    private String userId;

    @Ignore
    private List<Song> songs;

    public Playlist(String name, String userId) {
        this.name = name;
        this.userId = userId;
        this.songs = new ArrayList<>();
    }

    // Construtor vazio para o Room
    @Ignore
    public Playlist(String name) {
        this.name = name;
        this.songs = new ArrayList<>();
    }

    // Getters e Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Song> getSongs() {
        if (songs == null) {
            songs = new ArrayList<>();
        }
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public void addSong(Song song) {
        if (getSongs() != null && !getSongs().contains(song)) {
            getSongs().add(song);
        }
    }
}
