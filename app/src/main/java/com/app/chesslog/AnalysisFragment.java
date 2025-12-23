package com.app.chesslog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.app.chesslog.data.ChessGame;
import com.app.chesslog.databinding.FragmentAnalysisBinding;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AnalysisFragment extends Fragment {

    private FragmentAnalysisBinding binding;
    private MainViewModel viewModel;
    private Board board;
    private Game game;
    private int currentMoveIndex = 0;

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

        viewModel.getSelectedGame().observe(getViewLifecycleOwner(), chessGame -> {
            if (chessGame != null) {
                binding.whitePlayerNameLabel.setText("⚪ " + chessGame.whitePlayer);
                binding.blackPlayerNameLabel.setText("⚫ " + chessGame.blackPlayer);
                binding.pgnTextView.setText(chessGame.pgn);
                binding.engineAnalysisTextView.setText("Engine Analysis: Initializing...");

                try {
                    PgnHolder pgn = new PgnHolder(null);
                    pgn.loadPgn(chessGame.pgn);
                    if (!pgn.getGames().isEmpty()) {
                        game = pgn.getGames().get(0);
                        game.loadMoveText();
                        board = new Board(); // Reset board to initial state
                        currentMoveIndex = 0; // Reset move index
                        binding.boardView.setBoard(board); // Set initial board
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        binding.forwardMoveButton.setOnClickListener(v -> {
            if (board != null && game != null) {
                MoveList moves = game.getHalfMoves();
                if (currentMoveIndex < moves.size()) {
                    board.doMove(moves.get(currentMoveIndex));
                    currentMoveIndex++;
                    binding.boardView.setBoard(board); // Update board view
                }
            }
        });

        binding.backMoveButton.setOnClickListener(v -> {
            if (board != null && currentMoveIndex > 0) {
                board.undoMove();
                currentMoveIndex--;
                binding.boardView.setBoard(board); // Update board view
            }
        });

        binding.saveGameButton.setOnClickListener(v -> {
            ChessGame selectedGame = viewModel.getSelectedGame().getValue();
            if (selectedGame != null) {
                viewModel.insertGame(selectedGame);
                Toast.makeText(getContext(), "Game saved!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}