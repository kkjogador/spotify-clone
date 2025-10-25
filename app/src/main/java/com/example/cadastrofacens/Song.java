package com.example.cadastrofacens;

import java.io.Serializable;

public class Song implements Serializable {
    private String title;
    private String artist;
    private int fileResId;
    private boolean isSaved = false; // Novo campo

    public Song(String title, String artist, int fileResId) {
        this.title = title;
        this.artist = artist;
        this.fileResId = fileResId;
    }

    // Getters
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public int getFileResId() { return fileResId; }
    public boolean isSaved() { return isSaved; }

    // Setter
    public void setSaved(boolean saved) { isSaved = saved; }
}
