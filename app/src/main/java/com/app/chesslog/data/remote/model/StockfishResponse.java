package com.app.chesslog.data.remote.model;

import com.google.gson.annotations.SerializedName;

public class StockfishResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private String data;

    @SerializedName("bestmove")
    private String bestmove;

    @SerializedName("continuation")
    private String continuation;

    @SerializedName("evaluation")
    private Double evaluation;

    @SerializedName("mate")
    private Integer mate;

    public boolean isSuccess() {
        return success;
    }

    public String getData() {
        return data;
    }

    public String getBestmove() {
        return bestmove;
    }

    public String getContinuation() {
        return continuation;
    }

    public Double getEvaluation() {
        return evaluation;
    }

    public Integer getMate() {
        return mate;
    }
}
