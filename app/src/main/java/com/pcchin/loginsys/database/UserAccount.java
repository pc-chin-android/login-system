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

    public UserAccount(int userId, String username, String creationDate, String firstName,
                       String lastName, String salt, String passhash, String codeHash,
                       String birthday, String photo) {
        this.userId = userId;
        this.username = username;
        this.creationDate = creationDate;
        this.firstName = firstName;
        this.lastName = lastName;
        this.salt = salt;
        this.passhash = passhash;
        this.codehash = codeHash;
        this.birthday = birthday;
        this.photo = photo;
    }

    UserAccount() {}
}
