package com.example.cadastrofacens;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Serializable permite que o objeto seja passado entre activities/fragments
public class Playlist implements Serializable {
    private String name;
    private List<Song> songs;

    public Playlist(String name) {
        this.name = name;
        this.songs = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void addSong(Song song) {
        if (!songs.contains(song)) {
            songs.add(song);
        }
    }
}
