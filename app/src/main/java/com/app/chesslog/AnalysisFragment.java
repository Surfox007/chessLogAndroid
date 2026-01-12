package com.app.chesslog;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.chesslog.data.ChessGame;
import com.app.chesslog.databinding.FragmentAnalysisBinding;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;

public class AnalysisFragment extends Fragment {

    private FragmentAnalysisBinding binding;
    private MainViewModel viewModel;
    private Board board;
    private Game game;
    private int currentMoveIndex = -1; // Start at -1 (before first move)
    private MoveListAdapter moveListAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAnalysisBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // Start Stockfish engine and set up its observers first to prevent race conditions
        String enginePath = MainActivity.extractAndGetEnginePath(requireContext());
        if (enginePath != null) {
            viewModel.startEngine(enginePath);
        } else {
            binding.stockfishAnalysisPlaceholder.setText("Engine not found or failed to extract.");
        }

        viewModel.getEngineAnalysis().observe(getViewLifecycleOwner(), analysis -> {
            if (analysis == null || analysis.isEmpty()) {
                binding.stockfishAnalysisPlaceholder.setText("");
                return;
            }

            SpannableString spannable = new SpannableString(analysis);
            int firstSpace = analysis.indexOf(" ");
            if (firstSpace != -1) {
                // Highlight Evaluation
                spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, firstSpace, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, firstSpace, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new BackgroundColorSpan(Color.DKGRAY), 0, firstSpace, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            binding.stockfishAnalysisPlaceholder.setText(spannable);
        });

        viewModel.isEngineEnabled().observe(getViewLifecycleOwner(), isEnabled -> {
            binding.toolbar.getMenu().findItem(R.id.action_toggle_engine).setChecked(isEnabled);
            if (isEnabled) {
                // Trigger analysis for the current position when engine is turned on
                if(board != null) {
                    viewModel.analyzeCurrentPosition(board.getFen());
                }
            }
        });

        setupRecyclerView();

        // Setup board move listener
        binding.boardView.setOnMoveListener(move -> {
            if (game == null || board == null) return;

            try {
                // Check legality using the board
                if (board.doMove(move)) {
                    // If we are not at the end of the game, truncate the future moves
                    if (currentMoveIndex < game.getHalfMoves().size() - 1) {
                         // Remove moves from currentMoveIndex + 1 to end
                         int movesToKeep = currentMoveIndex + 1;
                         MoveList moves = game.getHalfMoves();
                         while (moves.size() > movesToKeep) {
                             moves.remove(moves.size() - 1);
                         }
                    }
                    
                    game.getHalfMoves().add(move);
                    moveListAdapter.setMoves(game.getHalfMoves());
                    updateBoardPosition(game.getHalfMoves().size());
                } else {
                    Toast.makeText(getContext(), "Illegal move", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getSelectedGame().observe(getViewLifecycleOwner(), chessGame -> {
            if (chessGame != null) {
                binding.whitePlayerNameLabel.setText("⚪ " + chessGame.whitePlayer);
                binding.blackPlayerNameLabel.setText("⚫ " + chessGame.blackPlayer);

                try {
                    PgnHolder pgn = new PgnHolder(null);
                    pgn.loadPgn(chessGame.pgn);
                    if (!pgn.getGames().isEmpty()) {
                        game = pgn.getGames().get(0);
                        game.loadMoveText();
                        MoveList moves = game.getHalfMoves();
                        moveListAdapter.setMoves(moves);

                        board = new Board();
                        updateBoardPosition(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    binding.stockfishAnalysisPlaceholder.setText("Error loading game: " + e.getMessage());
                }
            } else {
                setupNewGame();
            }
        });

        setupNavigationListeners();
        setupToolbarListener();
    }

    private void setupNewGame() {
        try {
            PgnHolder pgn = new PgnHolder(null);
            String pgnString = "[Event \"Casual Game\"]\n" +
                               "[Site \"Local\"]\n" +
                               "[Date \"" + new java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.US).format(new java.util.Date()) + "\"]\n" +
                               "[Round \"1\"]\n" +
                               "[White \"White\"]\n" +
                               "[Black \"Black\"]\n" +
                               "[Result \"*\"]\n";
            pgn.loadPgn(pgnString);
            if (pgn.getGames().size() > 0) {
                game = pgn.getGames().get(0);
                game.loadMoveText();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        board = new Board();
        
        binding.whitePlayerNameLabel.setText("⚪ White");
        binding.blackPlayerNameLabel.setText("⚫ Black");
        
        if (game != null) {
            moveListAdapter.setMoves(game.getHalfMoves());
        }
        updateBoardPosition(0);
    }

    private void setupRecyclerView() {
        moveListAdapter = new MoveListAdapter();
        binding.movesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.movesRecyclerView.setAdapter(moveListAdapter);

        moveListAdapter.setOnItemClickListener(moveIndex -> {
            updateBoardPosition(moveIndex + 1);
        });
    }

    private void setupNavigationListeners() {
        binding.forwardMoveButton.setOnClickListener(v -> {
            if (game != null && currentMoveIndex < game.getHalfMoves().size() - 1) {
                updateBoardPosition(currentMoveIndex + 2);
            }
        });

        binding.backMoveButton.setOnClickListener(v -> {
            if (game != null && currentMoveIndex >= 0) {
                updateBoardPosition(currentMoveIndex);
            }
        });

        binding.skipNextButton.setOnClickListener(v -> {
            if (game != null) {
                updateBoardPosition(game.getHalfMoves().size());
            }
        });

        binding.skipPreviousButton.setOnClickListener(v -> {
            if (game != null) {
                updateBoardPosition(0);
            }
        });
    }

    private void setupToolbarListener() {
        binding.toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_toggle_engine) {
                viewModel.toggleEngine();
                return true;
            } else if (itemId == R.id.action_flip_board) {
                binding.boardView.flip();
                return true;
            } else if (itemId == R.id.action_edit_game_info) {
                showEditGameInfoDialog();
                return true;
            } else if (itemId == R.id.action_edit_notes) {
                showEditNotesDialog();
                return true;
            } else if (itemId == R.id.action_save_game) {
                ChessGame selectedGame = viewModel.getSelectedGame().getValue();
                if (selectedGame == null) {
                    selectedGame = new ChessGame();
                    selectedGame.url = java.util.UUID.randomUUID().toString(); // Generate unique ID for local game
                    selectedGame.whitePlayer = "White";
                    selectedGame.blackPlayer = "Black";
                    viewModel.setSelectedGame(selectedGame);
                }
                
                if (game != null) {
                    selectedGame.pgn = game.toPgn(true, true);
                    viewModel.insertGame(selectedGame);
                    Toast.makeText(getContext(), "Game saved!", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
    }

    private void showEditNotesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Game Notes");

        final EditText input = new EditText(getContext());
        input.setHint("Enter your notes here...");
        input.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setMinLines(5);
        input.setPadding(32, 32, 32, 32);
        
        ChessGame selectedGame = viewModel.getSelectedGame().getValue();
        if (selectedGame != null && selectedGame.note != null) {
            input.setText(selectedGame.note);
        }

        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String note = input.getText().toString();
            
            ChessGame gameToUpdate = viewModel.getSelectedGame().getValue();
            if (gameToUpdate == null) {
                gameToUpdate = new ChessGame();
                gameToUpdate.url = java.util.UUID.randomUUID().toString();
                gameToUpdate.whitePlayer = "White";
                gameToUpdate.blackPlayer = "Black";
                viewModel.setSelectedGame(gameToUpdate);
            }
            
            gameToUpdate.note = note;
            // Persist changes immediately
            viewModel.insertGame(gameToUpdate);
            Toast.makeText(getContext(), "Note saved!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showEditGameInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Game Info");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        final EditText whitePlayerInput = new EditText(getContext());
        whitePlayerInput.setHint("White Player Name");
        if (viewModel.getSelectedGame().getValue() != null) {
            whitePlayerInput.setText(viewModel.getSelectedGame().getValue().whitePlayer);
        } else {
            whitePlayerInput.setText("White");
        }
        layout.addView(whitePlayerInput);

        final EditText blackPlayerInput = new EditText(getContext());
        blackPlayerInput.setHint("Black Player Name");
        if (viewModel.getSelectedGame().getValue() != null) {
            blackPlayerInput.setText(viewModel.getSelectedGame().getValue().blackPlayer);
        } else {
             blackPlayerInput.setText("Black");
        }
        layout.addView(blackPlayerInput);

        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String white = whitePlayerInput.getText().toString().trim();
            String black = blackPlayerInput.getText().toString().trim();

            if (white.isEmpty()) white = "White";
            if (black.isEmpty()) black = "Black";

            // Update UI
            binding.whitePlayerNameLabel.setText("⚪ " + white);
            binding.blackPlayerNameLabel.setText("⚫ " + black);

            // Update ViewModel/Game object
            ChessGame selected = viewModel.getSelectedGame().getValue();
            if (selected == null) {
                selected = new ChessGame();
                selected.url = java.util.UUID.randomUUID().toString();
                selected.whitePlayer = "White"; 
                selected.blackPlayer = "Black";
                viewModel.setSelectedGame(selected);
            }
            selected.whitePlayer = white;
            selected.blackPlayer = black;
            
            // Note: We don't update the internal 'game' object's tags here because 
            // the 'game' object is primarily for moves/board state. 
            // The PGN generation might miss these tags if we don't set them, 
            // but for now we rely on the ChessGame entity holding the names.
            // Ideally, we should update game metadata too if supported.
            
            Toast.makeText(getContext(), "Info updated. Don't forget to Save Game.", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateBoardPosition(int targetMoveNumber) {
        if (game == null) return;

        board.loadFromFen(new Board().getFen()); // Reset to start
        MoveList moves = game.getHalfMoves();

        int movesToApply = Math.min(targetMoveNumber, moves.size());

        for (int i = 0; i < movesToApply; i++) {
            board.doMove(moves.get(i));
        }
        currentMoveIndex = movesToApply - 1;

        binding.boardView.setBoard(board);
        moveListAdapter.setSelectedMove(currentMoveIndex);

        // Trigger Stockfish analysis for the new position
        viewModel.analyzeCurrentPosition(board.getFen());

        // Scroll RecyclerView to the selected move
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.movesRecyclerView.getLayoutManager();
        if (layoutManager != null) {
            int scrollToPosition = (currentMoveIndex < 0) ? 0 : currentMoveIndex / 2;
            layoutManager.scrollToPositionWithOffset(scrollToPosition, 0);
        }
    }
}
