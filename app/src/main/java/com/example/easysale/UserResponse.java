package com.example.easysale;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserResponse {
    @SerializedName("total")
    private int total;

    @SerializedName("data")
    private List<User> data;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<User> getData() {
        return data;
    }

    public void setData(List<User> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "UserResponse{total=" + total + ", data=" + (data != null ? data.toString() : "null") + "}";
    }
}