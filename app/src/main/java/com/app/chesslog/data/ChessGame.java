package com.app.chesslog.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "saved_games")
public class ChessGame {
    @PrimaryKey
    @NonNull
    public String url;
    public String pgn;
    public String whitePlayer;
    public String blackPlayer;
}
