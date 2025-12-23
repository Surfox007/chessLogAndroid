package com.app.chesslog.data.remote.model;

import com.google.gson.annotations.SerializedName;

public class Game {
    @SerializedName("url")
    private String url;
    @SerializedName("pgn")
    private String pgn;
    @SerializedName("white")
    private Player white;
    @SerializedName("black")
    private Player black;

    public String getUrl() {
        return url;
    }

    public String getPgn() {
        return pgn;
    }

    public Player getWhite() {
        return white;
    }

    public Player getBlack() {
        return black;
    }
}