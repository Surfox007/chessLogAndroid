package com.app.chesslog;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.app.chesslog.data.AppDatabase;
import com.app.chesslog.data.ChessGame;
import com.app.chesslog.data.GameDao;
import com.app.chesslog.data.remote.ChessApiService;
import com.app.chesslog.data.remote.StockfishApiService;
import com.app.chesslog.data.remote.RetrofitClient;
import com.app.chesslog.data.remote.model.Archives;
import com.app.chesslog.data.remote.model.Game;
import com.app.chesslog.data.remote.model.Games;
import com.app.chesslog.data.remote.model.StockfishResponse;
import java.io.IOException; // Added for UCIEngine
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainViewModel extends AndroidViewModel {
    private static final String TAG = "MainViewModel";
    private final GameDao gameDao;
    private final LiveData<List<ChessGame>> savedGames;
    private final SingleLiveEvent<List<ChessGame>> importedGames = new SingleLiveEvent<>();
    private final MutableLiveData<ChessGame> selectedGame = new MutableLiveData<>();
    private final ChessApiService chessApiService;
    private final StockfishApiService stockfishApiService;

    // Stockfish integration
    private UCIEngine uciEngine;
    private final MutableLiveData<String> engineAnalysis = new MutableLiveData<>();
    private final MutableLiveData<List<String>> engineMoveList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isEngineEnabled = new MutableLiveData<>(false);

    public MainViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        gameDao = db.gameDao();
        savedGames = gameDao.getAllGames();
        chessApiService = RetrofitClient.getClient("https://api.chess.com/").create(ChessApiService.class);
        stockfishApiService = RetrofitClient.getClient("https://stockfish.online/").create(StockfishApiService.class);
    }

    public LiveData<List<String>> getEngineMoveList() {
        return engineMoveList;
    }

    public LiveData<List<ChessGame>> getSavedGames() {
        return savedGames;
    }

    public LiveData<List<ChessGame>> getImportedGames() {
        return importedGames;
    }

    public MutableLiveData<ChessGame> getSelectedGame() {
        return selectedGame;
    }

    public void setSelectedGame(ChessGame game) {
        selectedGame.setValue(game);
    }

    // Stockfish integration getters/setters/methods
    public LiveData<String> getEngineAnalysis() {
        return engineAnalysis;
    }

    public LiveData<Boolean> isEngineEnabled() {
        return isEngineEnabled;
    }

    public void toggleEngine() {
        boolean newState = Boolean.FALSE.equals(isEngineEnabled.getValue());
        isEngineEnabled.setValue(newState);
        if (!newState) {
            engineAnalysis.postValue("");
        }
    }

    public void startEngine(String enginePath) {
        if (uciEngine == null) {
            uciEngine = new UCIEngine();
            uciEngine.setOnBestMoveListener(bestMove -> {
                Log.d(TAG, "Best Move received: " + bestMove);
                // Parse bestMove (e.g., "bestmove e2e4 ponder e7e5") to show only the move
                String parsedMove = bestMove.replace("bestmove ", "").split(" ")[0];
                engineAnalysis.postValue("Best Move: " + parsedMove);
            });
            uciEngine.setOnInfoListener(info -> Log.d(TAG, "Engine Info: " + info));
            uciEngine.setOnErrorListener(error -> {
                Log.e(TAG, "Engine Error: " + error);
            });
            try {
                uciEngine.start(enginePath);
            } catch (IOException e) {
                Log.e(TAG, "Failed to start UCI Engine", e);
                uciEngine = null; // Discard broken engine instance
            }
        }
    }

    public void analyzeCurrentPosition(String fen) {
        if (Boolean.FALSE.equals(isEngineEnabled.getValue())) {
            engineAnalysis.postValue("");
            return;
        }

        // Try online API first as per user's request for "Online API Approach"
        analyzePositionOnline(fen);

        // If you still want to use the local engine as a fallback or in parallel:
        /*
        if (uciEngine != null) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    uciEngine.sendCommand("ucinewgame");
                    uciEngine.sendCommand("isready");
                    uciEngine.sendCommand("position fen " + fen);
                    uciEngine.sendCommand("go movetime 1000");
                } catch (Exception e) {
                    Log.e(TAG, "Error analyzing position locally: " + e.getMessage());
                }
            });
        }
        */
    }

    private void analyzePositionOnline(String fen) {
        stockfishApiService.getBestMove(fen, 10, "bestmove").enqueue(new Callback<StockfishResponse>() {
            @Override
            public void onResponse(Call<StockfishResponse> call, Response<StockfishResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StockfishResponse stockfishResponse = response.body();
                    if (stockfishResponse.isSuccess()) {
                        String evaluation = stockfishResponse.getEvaluation() != null ? 
                                String.format("%s%.2f", stockfishResponse.getEvaluation() > 0 ? "+" : "", stockfishResponse.getEvaluation()) : 
                                (stockfishResponse.getMate() != null ? "#" + stockfishResponse.getMate() : "0.00");
                        
                        String continuation = stockfishResponse.getContinuation() != null ? stockfishResponse.getContinuation() : "";
                        String analysis = evaluation + "  " + continuation;
                        engineAnalysis.postValue(analysis.trim());

                        if (stockfishResponse.getContinuation() != null) {
                            List<String> moves = java.util.Arrays.asList(stockfishResponse.getContinuation().split(" "));
                            engineMoveList.postValue(moves);
                        }
                    } else {
                        Log.e(TAG, "API Error: " + stockfishResponse.getData());
                    }
                } else {
                    Log.e(TAG, "API Call failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<StockfishResponse> call, Throwable t) {
                Log.e(TAG, "Failed to analyze position online", t);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (uciEngine != null) {
            uciEngine.stop();
        }
    }

    public void fetchGames(String username) {
        Log.d(TAG, "Fetching games for username: " + username);
        chessApiService.getArchives(username).enqueue(new Callback<Archives>() {
            @Override
            public void onResponse(Call<Archives> call, Response<Archives> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully fetched archives");
                    List<String> archiveUrls = response.body().getArchives();
                    if (archiveUrls != null && !archiveUrls.isEmpty()) {
                        // For simplicity, fetch games from the last archive only
                        String lastArchiveUrl = archiveUrls.get(archiveUrls.size() - 1);
                        Log.d(TAG, "Fetching games from archive: " + lastArchiveUrl);
                        fetchGamesFromArchive(lastArchiveUrl);
                    } else {
                        Log.d(TAG, "No archives found for user: " + username);
                    }
                } else {
                    Log.e(TAG, "Failed to fetch archives. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Archives> call, Throwable t) {
                Log.e(TAG, "Failed to fetch archives", t);
            }
        });
    }

    private void fetchGamesFromArchive(String url) {
        chessApiService.getGames(url).enqueue(new Callback<Games>() {
            @Override
            public void onResponse(Call<Games> call, Response<Games> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully fetched games from archive");
                    List<ChessGame> games = new ArrayList<>();
                    for (Game game : response.body().getGames()) {
                        ChessGame chessGame = new ChessGame();
                        chessGame.url = game.getUrl();
                        chessGame.pgn = game.getPgn();
                        chessGame.whitePlayer = game.getWhite().getUsername();
                        chessGame.blackPlayer = game.getBlack().getUsername();
                        games.add(chessGame);
                    }
                    importedGames.postValue(games);
                } else {
                    Log.e(TAG, "Failed to fetch games from archive. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Games> call, Throwable t) {
                Log.e(TAG, "Failed to fetch games from archive", t);
            }
        });
    }

    public void insertGame(ChessGame game) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            gameDao.insert(game);
        });
    }

    public void deleteGame(ChessGame game) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            gameDao.delete(game.url);
        });
    }
}