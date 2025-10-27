package com.example.cadastrofacens;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {

    @PrimaryKey
    @NonNull
    private String id; // ID do Google Sign-In

    private String displayName;
    private String email;
    private String photoUrl;

    // É necessário um construtor vazio para o Room
    public User() {}

    public User(@NonNull String id, String displayName, String email, String photoUrl) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.photoUrl = photoUrl;
    }

    // Getters
    @NonNull
    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    // Setters
    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
