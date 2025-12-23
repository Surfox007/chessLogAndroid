package com.app.chesslog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.app.chesslog.data.ChessGame;
import com.app.chesslog.databinding.ListItemGameBinding;

public class GameListAdapter extends ListAdapter<ChessGame, GameListAdapter.GameViewHolder> {

    private final OnItemClickListener listener;
    private final boolean showDeleteButton;

    public GameListAdapter(boolean showDeleteButton, OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.showDeleteButton = showDeleteButton;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemGameBinding binding = ListItemGameBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new GameViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        ChessGame currentGame = getItem(position);
        holder.bind(currentGame, listener, showDeleteButton);
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        private final ListItemGameBinding binding;

        public GameViewHolder(ListItemGameBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final ChessGame game, final OnItemClickListener listener, boolean showDeleteButton) {
            binding.playersTextView.setText(game.whitePlayer + " vs. " + game.blackPlayer);
            binding.resultTextView.setText(""); // PGN does not contain the result in a simple format
            binding.dateTextView.setText(""); // PGN does not contain the date in a simple format

            if (showDeleteButton) {
                binding.deleteButton.setVisibility(View.VISIBLE);
                binding.deleteButton.setOnClickListener(v -> listener.onDeleteClick(game));
                binding.loadButton.setVisibility(View.GONE); // Hide load button if delete is shown
            } else {
                binding.deleteButton.setVisibility(View.GONE);
                binding.loadButton.setVisibility(View.VISIBLE); // Show load button if delete is hidden
                binding.loadButton.setOnClickListener(v -> listener.onLoadClick(game));
            }

            itemView.setOnClickListener(v -> listener.onItemClick(game));
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ChessGame game);
        void onDeleteClick(ChessGame game);
        void onLoadClick(ChessGame game);
    }

    private static final DiffUtil.ItemCallback<ChessGame> DIFF_CALLBACK = new DiffUtil.ItemCallback<ChessGame>() {
        @Override
        public boolean areItemsTheSame(@NonNull ChessGame oldItem, @NonNull ChessGame newItem) {
            return oldItem.url.equals(newItem.url);
        }

        @Override
        public boolean areContentsTheSame(@NonNull ChessGame oldItem, @NonNull ChessGame newItem) {
            return oldItem.pgn.equals(newItem.pgn);
        }
    };
}