package com.app.chesslog.data.remote.model;

import com.google.gson.annotations.SerializedName;

public class Player {
    @SerializedName("username")
    private String username;

    public String getUsername() {
        return username;
    }
}