package com.example.easysale;

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
        Log.d(TAG, "setEmail: " + email);
    }

    // First Name
    public String getFirstName() {
        Log.d(TAG, "getFirstName: " + firstName);
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
        Log.d(TAG, "setFirstName: " + firstName);
    }

    // Last Name
    public String getLastName() {
        Log.d(TAG, "getLastName: " + lastName);
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
        Log.d(TAG, "setLastName: " + lastName);
    }

    // Avatar
    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
        Log.d(TAG, "setAvatar: " + avatar);
    }
}
