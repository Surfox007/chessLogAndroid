package com.app.chesslog;

import android.content.Intent;
import android.os.Bundle;
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

        setupRecyclerView();

        viewModel.getSelectedGame().observe(getViewLifecycleOwner(), chessGame -> {
            if (chessGame != null) {
                binding.whitePlayerNameLabel.setText("⚪ " + chessGame.whitePlayer);
                binding.blackPlayerNameLabel.setText("⚫ " + chessGame.blackPlayer);
                binding.stockfishAnalysisPlaceholder.setText("Stockfish analysis will be displayed here.");

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
                }
            }
        });

        setupNavigationListeners();
        setupToolbarListener();
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
            if (itemId == R.id.action_flip_board) {
                binding.boardView.flip();
                return true;
            } else if (itemId == R.id.action_save_game) {
                ChessGame selectedGame = viewModel.getSelectedGame().getValue();
                if (selectedGame != null) {
                    viewModel.insertGame(selectedGame);
                    Toast.makeText(getContext(), "Game saved!", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
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

        // Scroll RecyclerView to the selected move
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.movesRecyclerView.getLayoutManager();
        if (layoutManager != null) {
            int scrollToPosition = (currentMoveIndex < 0) ? 0 : currentMoveIndex / 2;
            layoutManager.scrollToPositionWithOffset(scrollToPosition, 0);
        }
    }
}
