package com.example.easysale.model;

import android.util.Log;
import java.io.Serializable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "users")
public class User implements Serializable {
    private static final String TAG = "User";

    @PrimaryKey
    private int id;

    @ColumnInfo(name = "email")
    private String email;

    @SerializedName("first_name")
    @ColumnInfo(name = "firstName")
    private String firstName;

    @SerializedName("last_name")
    @ColumnInfo(name = "lastName")
    private String lastName;

    @ColumnInfo(name = "avatar")
    private String avatar;

    // ID
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    // Email
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    // First Name
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    // Last Name
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // Avatar
    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
