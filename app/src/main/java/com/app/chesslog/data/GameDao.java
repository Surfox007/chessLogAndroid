package com.app.chesslog.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface GameDao {
    @Query("SELECT * FROM saved_games")
    LiveData<List<ChessGame>> getAllGames();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChessGame game);

    @Query("DELETE FROM saved_games WHERE url = :url")
    void delete(String url);
}
