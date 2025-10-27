package com.example.cadastrofacens;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateUser(User user);

    @Query("SELECT * FROM users WHERE id = :userId")
    User getUserById(String userId);
}
