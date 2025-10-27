package com.example.cadastrofacens;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

// Classe para agrupar uma Playlist com sua lista de Músicas
public class PlaylistWithSongs {
    @Embedded
    public Playlist playlist;

    @Relation(
            parentColumn = "id", // ID da Playlist
            entityColumn = "id", // ID da Song
            associateBy = @Junction(value = PlaylistSongCrossRef.class,
                                     parentColumn = "playlistId",
                                     entityColumn = "songId")
    )
    public List<Song> songs;
}
