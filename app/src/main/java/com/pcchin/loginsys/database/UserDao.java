package com.pcchin.loginsys.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM userAccount")
    List<UserAccount> getAllUser();

    @Query("SELECT * FROM userAccount WHERE _id = (:userId)")
    UserAccount searchById(int userId);

    @Query("SELECT * FROM userAccount WHERE _username = :username")
    UserAccount searchByUsername(String username);

    @Insert
    void insert(UserAccount user);

    @Update
    void update(UserAccount user);

    @Delete
    void delete(UserAccount user);
}
