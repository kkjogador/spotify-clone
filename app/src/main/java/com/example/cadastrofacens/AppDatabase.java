package com.example.cadastrofacens;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Adicionada a entidade PlayedSong e a versão do banco incrementada
@Database(entities = {User.class, Playlist.class, Song.class, PlaylistSongCrossRef.class, PlayedSong.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract PlaylistDao playlistDao();
    public abstract SongDao songDao();
    public abstract PlayedSongDao playedSongDao(); // Adicionado o DAO do histórico

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            .addCallback(sRoomDatabaseCallback)
                            .fallbackToDestructiveMigration() // Adicionado para lidar com a mudança de versão
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                SongDao dao = INSTANCE.songDao();
                if (dao.getSongCount() == 0) {
                    Song[] songs = {
                            new Song("MTG Fast Food", "Gordão do SN", R.raw.gordaodosn),
                            new Song("BH é o Califa", "MC Todynho BH", R.raw.mctodynhobhreal)
                    };
                    dao.insertAll(songs);
                }
            });
        }
    };
}
