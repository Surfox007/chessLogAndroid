package com.app.chesslog.data.remote;

import com.app.chesslog.data.remote.model.Archives;
import com.app.chesslog.data.remote.model.Games;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface ChessApiService {
    @GET("pub/player/{username}/games/archives")
    Call<Archives> getArchives(@Path("username") String username);

    @GET
    Call<Games> getGames(@Url String url);
}