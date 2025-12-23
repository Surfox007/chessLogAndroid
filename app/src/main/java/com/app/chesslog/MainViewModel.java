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
import com.app.chesslog.data.remote.RetrofitClient;
import com.app.chesslog.data.remote.model.Archives;
import com.app.chesslog.data.remote.model.Game;
import com.app.chesslog.data.remote.model.Games;
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

    public MainViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        gameDao = db.gameDao();
        savedGames = gameDao.getAllGames();
        chessApiService = RetrofitClient.getClient("https://api.chess.com/").create(ChessApiService.class);
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
