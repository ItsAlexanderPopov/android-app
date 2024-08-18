package com.example.easysale;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String email;
    private String first_name;
    private String last_name;
    private String avatar;

    // ID
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    // Email
    public String getEmail(){
        return email;
    }
    public void setEmail(String email){
        this.email = email;
    }

    // First Name
    public String getFirstName(){
        return first_name;
    }
    public void setFirstName(String first_name){
        this.first_name = first_name;
    }

    // Last Name
    public String getLastName(){
        return last_name;
    }
    public void setLastName(String last_name){
        this.last_name = last_name;
    }

    // Avatar
    public String getAvatar(){
        return avatar;
    }
    public void setAvatar(String avatar){
        this.avatar = avatar;
    }

}