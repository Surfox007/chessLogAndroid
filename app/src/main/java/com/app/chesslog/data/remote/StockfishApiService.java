package com.app.chesslog.data.remote;

import com.app.chesslog.data.remote.model.StockfishResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StockfishApiService {
    @GET("api/s/v2.php")
    Call<StockfishResponse> getBestMove(
            @Query("fen") String fen,
            @Query("depth") int depth,
            @Query("mode") String mode
    );
}
