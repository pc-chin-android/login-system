package com.pcchin.loginsys.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class UserAccount {
    @PrimaryKey
    @ColumnInfo(name = "_id")
    public int userId;

    @ColumnInfo(name = "_username")
    public String username;

    @ColumnInfo(name = "creation_date")
    public String creationDate;

    @ColumnInfo(name = "first_name")
    public String firstName;

    @ColumnInfo(name = "last_name")
    public String lastName;

    @ColumnInfo(name = "salt")
    public String salt;

    @ColumnInfo(name = "passhash")
    public String passhash;

    @ColumnInfo(name = "codehash")
    public String codehash;

    @ColumnInfo(name = "birthday")
    public String birthday;

    @ColumnInfo(name = "photolink")
    public String photo;
}
